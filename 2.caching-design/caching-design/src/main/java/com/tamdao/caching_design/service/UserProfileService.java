package com.tamdao.caching_design.service;

import com.tamdao.caching_design.dto.UpdateUserRequest;
import com.tamdao.caching_design.event.UserProfileUpdatedEvent;
import com.tamdao.caching_design.model.UserProfile;
import com.tamdao.caching_design.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * =============================================================================
 *  CHIẾN LƯỢC: Read-Aside + Delete Write-Around (RA + DWA)
 * =============================================================================
 *
 *  ┌───────────────────────────────────────────────────────────────────────┐
 *  │  LUỒNG ĐỌC (Read-Aside)                                               │
 *  │                                                                        │
 *  │  App → Check Cache → [HIT] → Trả về ngay                             │
 *  │                    → [MISS] → Query DB → Write Back vào Cache         │
 *  │                                         → Trả về                     │
 *  └───────────────────────────────────────────────────────────────────────┘
 *
 *  ┌───────────────────────────────────────────────────────────────────────┐
 *  │  LUỒNG GHI (Delete Write-Around)                                      │
 *  │                                                                        │
 *  │  App → UPDATE DB (bước 1 - chậm, an toàn)                            │
 *  │      → DELETE Cache (bước 2 - nhanh)                                  │
 *  │      → Trả về thành công                                               │
 *  │                                                                        │
 *  │  → "Delete Cache LATER" là chiến lược được chọn (README 2.7.3)       │
 *  └───────────────────────────────────────────────────────────────────────┘
 *
 *  TẠI SAO "Delete Cache Later" tốt hơn "Delete Cache First"?
 *  → Xem README section 2.7.3 để hiểu phân tích xác suất Race Condition
 *
 *  BIỆN PHÁP GIẢM NHẸ (Mitigation - README 2.7.4):
 *  → Luôn gán TTL ngắn cho mọi entry trong Cache
 *  → Nếu Race Condition xảy ra, dữ liệu sai sẽ "bốc hơi" sau TTL
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * ApplicationEventPublisher: Spring's built-in event bus.
     *
     * Dùng để publish UserProfileUpdatedEvent vào trong application context.
     * CacheInvalidationService sẽ lắng nghe event này qua @TransactionalEventListener.
     *
     * → Đây là cơ chế triển khai CDC pattern (Solution 2 - README 3.1.1)
     *   trong phạm vi một JVM (in-process CDC).
     */
    private final ApplicationEventPublisher eventPublisher;

    /**
     * TTL cơ bản (Base TTL): 5 phút.
     *
     * SOLUTION 4 — Cache Avalanche / Đáo hạn diện rộng:
     * Không bao giờ dùng TTL cố định cho hàng loạt key được ghi cùng lúc.
     * Thay vào đó dùng TTL = BASE + random(0, JITTER) → xem buildTtlWithJitter().
     *
     * MITIGATION (README 2.7.4):
     * TTL ngắn cũng là biện pháp giảm nhẹ cho Race Condition.
     */
    private static final Duration CACHE_TTL_BASE = Duration.ofMinutes(5);

    /**
     * Khoảng Jitter tối đa cộng thêm vào TTL: 0 → 120 giây (2 phút).
     * Kết quả: mỗi key sẽ có TTL trong khoảng [5m, 7m] — không key nào giống key nào.
     */
    private static final Duration CACHE_TTL_JITTER = Duration.ofMinutes(2);

    /**
     * Cache key pattern: "user:{id}"
     *
     * Best Practice: Dùng prefix theo namespace (ví dụ: "user:") để:
     *  - Dễ phân biệt loại data (user, product, order...)
     *  - Dễ xóa theo pattern khi cần
     *  - Tránh xung đột key giữa các module
     */
    private static final String CACHE_KEY_PREFIX = "user:";

    // =========================================================================
    // LUỒNG ĐỌC — Read-Aside Strategy (README section 2.1.1)
    // =========================================================================

    /**
     * Lấy UserProfile theo ID — áp dụng chiến lược Read-Aside.
     *
     * Luồng thực thi:
     *   1. Tạo cache key từ userId (ví dụ: "user:1")
     *   2. Kiểm tra Redis → nếu có dữ liệu: CACHE HIT → trả về ngay
     *   3. Nếu Redis trả về null: CACHE MISS → query xuống H2/MySQL
     *   4. Ghi kết quả từ DB lên Redis kèm TTL (Write-Back)
     *   5. Trả về dữ liệu cho caller
     *
     * @param userId ID của user cần lấy
     * @return Optional<UserProfile>
     */
    public Optional<UserProfile> getUserProfile(Long userId) {
        String cacheKey = buildCacheKey(userId);

        // ─── BƯỚC 1: Kiểm tra Cache (Redis) ───────────────────────────────
        // redisTemplate.opsForValue() → thao tác với kiểu String (GET/SET)
        Object cachedValue = redisTemplate.opsForValue().get(cacheKey);

        if (cachedValue != null) {
            // ─── CACHE HIT ───────────────────────────────────────────────
            // Dữ liệu có trong Cache → trả về ngay, không cần xuống DB
            log.debug("[CACHE HIT] userId={}, key={}", userId, cacheKey);
            return Optional.of((UserProfile) cachedValue);
        }

        // ─── CACHE MISS ──────────────────────────────────────────────────
        // Không tìm thấy trong Cache → xuống DB
        log.debug("[CACHE MISS] userId={}, key={} → querying DB", userId, cacheKey);

        Optional<UserProfile> userFromDb = userProfileRepository.findById(userId);

        // ─── BƯỚC 2: Write-Back với JITTER TTL ───────────────────────────
        //
        // SOLUTION 4 — Tránh Đáo hạn diện rộng (Mass TTL Expiry):
        //
        // Nếu ta dùng CACHE_TTL = 5 phút cố định cho TẤT CẢ request:
        //   - t=08:00: 100 user GET đồng thời → 100 key ghi vào cache, TTL = 5m
        //   - t=08:05: 100 key đồng loạt hết hạn → 100 Cache Miss → 100 query DB
        //   → Cache Avalanche nguyên nhân 2!
        //
        // Với Jitter (base=5m, jitter=0~2m):
        //   - t=08:00: Key A → TTL=5m47s | Key B → TTL=6m12s | Key C → TTL=5m03s
        //   - Các key hết hạn RẢI ĐỀU từ 08:05 đến 08:07
        //   → Tối đa ~1 key/giây hết hạn thay vì 100 key cùng lúc ✅
        userFromDb.ifPresent(user -> {
            Duration jitteredTtl = buildTtlWithJitter();
            redisTemplate.opsForValue().set(cacheKey, user, jitteredTtl);
            log.debug("[CACHE WRITE-BACK] userId={}, key={}, TTL={}s (jitter applied)",
                    userId, cacheKey, jitteredTtl.toSeconds());
        });

        return userFromDb;
    }

    // =========================================================================
    // LUỒNG GHI — Delete Write-Around Strategy (README section 2.6 + 2.7)
    // =========================================================================

    /**
     * Cập nhật UserProfile — áp dụng chiến lược Delete Write-Around (Delete Cache Later).
     *
     * ┌──────────────────────────────────────────────────────────────────────┐
     * │  TẠI SAO "Update DB trước, Delete Cache sau"?                        │
     * │                                                                      │
     * │  Như phân tích trong README 2.7.1 vs 2.7.2:                         │
     * │                                                                      │
     * │  ❌ Delete Cache FIRST (sai):                                        │
     * │     Khoảng thời gian trống (từ lúc xóa cache đến lúc DB done)       │
     * │     rất DÀI (vì Update DB chậm). Luồng Read dễ dàng chạy vào        │
     * │     trong khoảng thời gian này, đọc data cũ từ DB và ghi ngược      │
     * │     lên Cache → Cache sai.                                           │
     * │                                                                      │
     * │  ✅ Delete Cache LATER (đúng - được chọn):                           │
     * │     Update DB xong hết rồi mới Delete Cache. Khoảng thời gian       │
     * │     nguy hiểm chỉ là khoảnh khắc giữa "Read DB xong" và            │
     * │     "Write-Back lên Cache" của luồng Read. Khoảng này RẤT HẸP      │
     * │     vì ghi RAM nhanh hơn ghi đĩa cứng hàng nghìn lần.              │
     * └──────────────────────────────────────────────────────────────────────┘
     *
     * Luồng thực thi:
     *   1. Tìm user trong DB (ném exception nếu không tồn tại)
     *   2. Cập nhật thông tin user vào DB → @Transactional đảm bảo rollback nếu lỗi
     *   3. Xóa (Invalidate) cache entry của user này → "Delete Cache LATER"
     *   4. Lần đọc tiếp theo sẽ Cache Miss và nạp lại dữ liệu mới từ DB
     *
     * @param userId ID user cần cập nhật
     * @param request Dữ liệu mới
     * @return UserProfile đã được cập nhật
     */
    @Transactional
    public UserProfile updateUserProfile(Long userId, UpdateUserRequest request) {
        // ─── BƯỚC 1: Update DB (Ghi DB trước) ────────────────────────────
        UserProfile existingUser = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        existingUser.setName(request.name());
        existingUser.setAge(request.age());
        existingUser.setEmail(request.email());

        UserProfile savedUser = userProfileRepository.save(existingUser);
        log.debug("[DB UPDATE] userId={}, newAge={}", userId, savedUser.getAge());

        // ─── BƯỚC 2: Publish Event (CDC Pattern - Solution 2) ─────────────
        //
        // THAY ĐỔI QUAN TRỌNG SO VỚI CODE CŨ:
        // Trước: gọi thẳng redis.delete(cacheKey) ngay tại đây (trong @Transactional)
        // Sau:   chỉ publish 1 event nhỏ, việc xóa cache do listener xử lý
        //
        // TẠI SAO KHÔNG XÓA CACHE TRỰC TIẾP TRONG @TRANSACTIONAL?
        //
        // Tình huống nguy hiểm:
        //   [BEGIN TX]
        //   → db.save() ← DB chưa commit!
        //   → redis.delete() ← Cache bị xóa sớm
        //   → [LỖI XẢY RA]
        //   → [ROLLBACK] DB về dữ liệu cũ
        //   Kết quả: DB = cũ, Cache = rỗng → lần đọc tiếp lấy data cũ từ DB ghi ngược lên cache
        //
        // Với event + AFTER_COMMIT listener:
        //   [BEGIN TX]
        //   → db.save()
        //   → publish(event) ← chỉ enqueue event trong memory, chưa làm gì
        //   → [COMMIT] ← chắc chắn DB đã ghi xong
        //   → Listener kích hoạt: redis.delete() ← xóa cache an toàn
        //   Nếu rollback → listener KHÔNG chạy → cache không bị xóa oan
        //
        // CacheInvalidationService.onUserProfileUpdated() sẽ bắt event này.
        eventPublisher.publishEvent(new UserProfileUpdatedEvent(userId));
        log.debug("[EVENT PUBLISHED] UserProfileUpdatedEvent for userId={}", userId);

        return savedUser;
    }

    // =========================================================================
    // CÁCH CŨ — Để so sánh với RA + DWA + CDC ở trên
    // =========================================================================

    /**
     * ⚠️  LEGACY METHOD — Cách cũ (KHÔNG AN TOÀN) — CHỈ ĐỂ SO SÁNH
     * ─────────────────────────────────────────────────────────────────────────
     *
     * Đây là cách mà hầu hết developer viết lần đầu khi chưa biết về
     * No Atomicity Problem (README 3.1.1):
     *
     *   @Transactional
     *   void update() {
     *       db.save();           ← BƯỚC 1
     *       redis.delete();      ← BƯỚC 2: xóa cache TRONG transaction
     *   }
     *
     * VẤN ĐỀ: Cả 2 bước nằm trong cùng 1 @Transactional, nhưng chúng
     * KHÔNG có tính Nguyên tử (No Atomicity) với nhau vì:
     *
     *   - db.save() → thao tác với H2/MySQL (ACID, rollback được)
     *   - redis.delete() → thao tác với Redis (KHÔNG thuộc transaction của DB)
     *
     * Kịch bản lỗi xảy ra như thế nào?
     *
     *   [BEGIN TX]
     *   → db.save(age=26)         ✅ DB chuẩn bị commit
     *   → redis.delete("user:1")  ❌ Redis đột ngột timeout/down
     *                              → Cache vẫn còn age=25 (cũ)!
     *   → [COMMIT] DB lưu age=26
     *
     *   Kết quả: DB = 26 ✅  |  Cache = 25 ❌  |  Client đọc thấy 25!
     *
     * Kịch bản khác (rollback): nếu DB rollback sau khi cache đã bị xóa:
     *
     *   [BEGIN TX]
     *   → db.save(age=26)
     *   → redis.delete("user:1")  ✅ Cache bị xóa thành công
     *   → [LỖI] → DB ROLLBACK về age=25
     *
     *   Kết quả: DB = 25  |  Cache = rỗng
     *   → Lần đọc tiếp: Cache Miss → DB → age=25 → write-back lên cache
     *   → Lần này OK (may mắn), nhưng khoảng thời gian trống (cache rỗng)
     *     có thể gây Cache Stampede nếu có nhiều request đồng thời.
     *
     * SO SÁNH VỚI CÁCH MỚI:
     *
     *   ┌─────────────────────────────────┬──────────────────────────────────────┐
     *   │       LEGACY (cách cũ)          │       MODERN (CDC - cách mới)        │
     *   ├─────────────────────────────────┼──────────────────────────────────────┤
     *   │ redis.delete() trong @Tx        │ publishEvent() → AFTER_COMMIT        │
     *   │ Xóa cache TRƯỚC commit          │ Xóa cache SAU KHI commit xong        │
     *   │ Không retry khi Redis lỗi       │ @Retryable tự retry 3 lần            │
     *   │ Nếu Redis down → Cache stale    │ Nếu Redis down → retry 500/1000ms   │
     *   │ Nếu DB rollback → Cache sai     │ Nếu DB rollback → Cache không đổi   │
     *   └─────────────────────────────────┴──────────────────────────────────────┘
     *
     * API test: PUT /api/users/{id}/legacy
     *
     * @param userId ID user cần cập nhật
     * @param request Dữ liệu mới
     * @return UserProfile đã được cập nhật
     */
    @Transactional
    public UserProfile updateUserProfileLegacy(Long userId, UpdateUserRequest request) {
        String cacheKey = buildCacheKey(userId);
        log.warn("[LEGACY] Using old cache invalidation pattern for userId={}", userId);

        // ─── BƯỚC 1: Update DB ────────────────────────────────────────────
        UserProfile existingUser = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        existingUser.setName(request.name());
        existingUser.setAge(request.age());
        existingUser.setEmail(request.email());

        UserProfile savedUser = userProfileRepository.save(existingUser);
        log.warn("[LEGACY][DB UPDATE] userId={}, newAge={}", userId, savedUser.getAge());

        // ─── BƯỚC 2: Xóa Cache TRỰC TIẾP trong @Transactional ────────────
        //
        // ⚠️  ĐÂY LÀ VẤN ĐỀ: redis.delete() không thuộc về DB transaction.
        //
        // Nếu dòng này ném exception (Redis timeout, kết nối đứt...):
        //   - DB đã save() → sẽ COMMIT khi method kết thúc bình thường
        //   - Cache vẫn còn data cũ (vì delete failed)
        //   - Client sẽ đọc data cũ cho đến khi TTL hết
        //
        // Nếu dòng này thành công nhưng DB sau đó rollback:
        //   - Cache bị xóa oan → Cache Miss
        //   - Không có retry mechanism nào ở đây
        //
        // Trong happy path (mọi thứ OK) → hoạt động tốt.
        // Vấn đề chỉ xuất hiện ở edge case → khó reproduce, khó debug.
        Boolean deleted = redisTemplate.delete(cacheKey);
        log.warn("[LEGACY][CACHE DELETE] userId={}, key={}, deleted={} — " +
                 "⚠️ This runs INSIDE @Transactional, BEFORE DB commit!",
                 userId, cacheKey, deleted);

        return savedUser;
    }



    /**
     * Tạo UserProfile mới.
     *
     * Khi tạo mới, không cần tương tác với Cache vì:
     *  - Chưa có cache entry nào cho user mới này
     *  - Cache sẽ được populate khi user đầu tiên gọi GET
     *    (Lazy Loading — chỉ cache khi có người hỏi)
     *
     * @param user UserProfile cần tạo
     * @return UserProfile vừa được lưu (đã có ID từ DB)
     */
    @Transactional
    public UserProfile createUserProfile(UserProfile user) {
        UserProfile saved = userProfileRepository.save(user);
        log.debug("[DB CREATE] userId={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    // =========================================================================
    // LUỒNG XÓA — Delete (phải invalidate cache)
    // =========================================================================

    /**
     * Xóa UserProfile khỏi hệ thống.
     *
     * Phải xóa cache entry tương ứng để tránh trường hợp:
     * - User đã bị xóa khỏi DB
     * - Nhưng cache vẫn còn → lần sau GET vẫn trả về "ghost data"
     *
     * Thứ tự: Delete DB trước → Delete Cache sau (nhất quán với Write strategy).
     *
     * @param userId ID user cần xóa
     */
    @Transactional
    public void deleteUserProfile(Long userId) {
        // Bước 1: Xóa khỏi DB
        userProfileRepository.deleteById(userId);
        log.debug("[DB DELETE] userId={}", userId);

        // Bước 2: Publish event → CDC listener xóa cache sau AFTER_COMMIT
        // Áp dụng cùng pattern như updateUserProfile() để đảm bảo nhất quán.
        eventPublisher.publishEvent(new UserProfileUpdatedEvent(userId));
        log.debug("[EVENT PUBLISHED] UserProfileUpdatedEvent (delete) for userId={}", userId);
    }

    // =========================================================================
    // HELPER
    // =========================================================================

    /**
     * Tạo cache key theo pattern chuẩn.
     */
    private String buildCacheKey(Long userId) {
        return CACHE_KEY_PREFIX + userId;
    }

    /**
     * Tạo TTL có Jitter ngẫu nhiên — Solution 4 (Cache Avalanche / Mass Expiry).
     *
     * Công thức: TTL = CACHE_TTL_BASE + random(0, CACHE_TTL_JITTER)
     *
     * Ví dụ với BASE=5min, JITTER=2min:
     *   Lần 1: 5m + 73s = 6m13s
     *   Lần 2: 5m + 8s  = 5m08s
     *   Lần 3: 5m + 117s = 6m57s
     *   → Mỗi key sống một khoảng thời gian khác nhau → không đáo hạn cùng lúc
     *
     * @return Duration ngẫu nhiên trong khoảng [BASE_TTL, BASE_TTL + MAX_JITTER]
     */
    private Duration buildTtlWithJitter() {
        long jitterSeconds = ThreadLocalRandom.current().nextLong(0, CACHE_TTL_JITTER.toSeconds());
        return CACHE_TTL_BASE.plusSeconds(jitterSeconds);
    }
}
