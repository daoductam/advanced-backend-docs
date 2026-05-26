package com.tamdao.caching_design.controller;

import com.tamdao.caching_design.dto.UpdateUserRequest;
import com.tamdao.caching_design.model.UserProfile;
import com.tamdao.caching_design.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller expose các API endpoint để test chiến lược cache.
 *
 * Base URL: http://localhost:8080/api/users
 *
 * Các endpoint:
 *   POST   /api/users              → Tạo user mới
 *   GET    /api/users/{id}         → Lấy user (Read-Aside)
 *   PUT    /api/users/{id}         → Cập nhật user (Delete Write-Around)
 *   DELETE /api/users/{id}         → Xóa user
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * Tạo user mới.
     * Curl: POST http://localhost:8080/api/users
     *       Body: { "name": "Tam Dao", "age": 25, "email": "tam@mail.com" }
     */
    @PostMapping
    public ResponseEntity<UserProfile> createUser(@RequestBody UserProfile user) {
        UserProfile created = userProfileService.createUserProfile(user);
        return ResponseEntity.ok(created);
    }

    /**
     * Lấy user theo ID — đây là nơi Read-Aside xảy ra.
     *
     * Lần 1: Cache Miss → query DB → write back cache → trả về (chậm)
     * Lần 2: Cache Hit  → trả về ngay từ Redis         (cực nhanh)
     *
     * Quan sát log khi gọi API nhiều lần:
     *   [CACHE MISS] → lần 1
     *   [CACHE HIT]  → lần 2, 3, 4...
     *
     * Curl: GET http://localhost:8080/api/users/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable Long id) {
        return userProfileService.getUserProfile(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Cập nhật user — đây là nơi Delete Write-Around xảy ra.
     *
     * Sau khi PUT:
     *   1. DB được cập nhật giá trị mới
     *   2. Event được publish → AFTER_COMMIT listener xóa cache
     * GET tiếp theo sẽ Cache Miss và lấy giá trị mới từ DB.
     *
     * Curl: PUT http://localhost:8080/api/users/1
     *       Body: { "name": "Tam Dao Updated", "age": 26, "email": "new@mail.com" }
     *
     * Log quan sát:
     *   [DB UPDATE] ...
     *   [EVENT PUBLISHED] ...
     *   ── sau khi transaction commit ──
     *   [CDC LISTENER] Transaction committed. Invalidating cache...
     *   [CACHE INVALIDATE] key=user:1 deleted=true
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserProfile> updateUserProfile(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request) {
        UserProfile updated = userProfileService.updateUserProfile(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * ⚠️  LEGACY endpoint — Cách cũ: xóa cache TRỰC TIẾP trong @Transactional.
     *
     * Dùng để SO SÁNH với endpoint PUT /{id} ở trên.
     *
     * Sự khác biệt trong log:
     *
     *   PUT /api/users/1 (MODERN):
     *     [DB UPDATE] userId=1, newAge=26
     *     [EVENT PUBLISHED] UserProfileUpdatedEvent for userId=1
     *     ── transaction commits ──
     *     [CDC LISTENER] Transaction committed. Invalidating cache...
     *     [CACHE INVALIDATE] key=user:1 deleted=true
     *
     *   PUT /api/users/1/legacy (LEGACY):
     *     [LEGACY] Using old cache invalidation pattern for userId=1
     *     [LEGACY][DB UPDATE] userId=1, newAge=26
     *     [LEGACY][CACHE DELETE] key=user:1, deleted=true ⚠️ INSIDE @Transactional, BEFORE commit!
     *     ── transaction commits ──  ← cache đã bị xóa TRƯỚC bước này!
     *
     * Nhận xét: Trong happy path cả 2 đều cho kết quả giống nhau.
     * Sự khác biệt chỉ lộ ra khi Redis lỗi hoặc DB rollback.
     *
     * Curl: PUT http://localhost:8080/api/users/1/legacy
     *       Body: { "name": "Tam Dao Legacy", "age": 27, "email": "legacy@mail.com" }
     */
    @PutMapping("/{id}/legacy")
    public ResponseEntity<UserProfile> updateUserProfileLegacy(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request) {
        UserProfile updated = userProfileService.updateUserProfileLegacy(id, request);
        return ResponseEntity.ok(updated);
    }



    /**
     * Xóa user khỏi DB và Cache.
     * Curl: DELETE http://localhost:8080/api/users/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserProfile(@PathVariable Long id) {
        userProfileService.deleteUserProfile(id);
        return ResponseEntity.noContent().build();
    }
}
