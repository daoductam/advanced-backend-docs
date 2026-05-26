package com.tamdao.caching_design.controller;

import com.tamdao.caching_design.model.UserProfile;
import com.tamdao.caching_design.service.CacheAvalancheService;
import com.tamdao.caching_design.service.CacheBreakdownService;
import com.tamdao.caching_design.service.CacheWarmingService;
import com.tamdao.caching_design.service.CacheHotKeyService;
import com.tamdao.caching_design.service.CacheLargeKeyService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller cho các API liên quan đến Cache Management.
 *
 * Problem 02 - Cache Avalanche endpoints:
 *   GET  /api/users/{id}/safe       → Circuit Breaker + TTL Jitter
 *   GET  /api/cache/circuit-breaker → Trạng thái Circuit Breaker
 *   POST /api/cache/warm-up         → Cache Warming thủ công
 *
 * Problem 03 - Cache Breakdown endpoints:
 *   GET /api/users/{id}/hotspot-no-ttl  → Solution 1: Không set TTL
 *   GET /api/users/{id}/hotspot-mutex   → Solution 3: Mutex Lock
 *   GET /api/cache/ttl/{userId}         → Xem TTL còn lại của key
 *   (Solution 2 Background Job chạy tự động - không cần API)
 */
@RestController
@RequiredArgsConstructor
public class CacheManagementController {

    private final CacheAvalancheService cacheAvalancheService;
    private final CacheWarmingService cacheWarmingService;
    private final CacheBreakdownService cacheBreakdownService;
    private final CacheHotKeyService cacheHotKeyService;
    private final CacheLargeKeyService cacheLargeKeyService;

    // =========================================================================
    // Problem 02 - Cache Avalanche APIs
    // =========================================================================

    @GetMapping("/api/users/{id}/safe")
    public ResponseEntity<UserProfile> getUserSafe(@PathVariable Long id) {
        Optional<UserProfile> user = cacheAvalancheService.getUserWithAvalancheProtection(id);
        return user.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.status(503).build());
    }

    @GetMapping("/api/cache/circuit-breaker")
    public ResponseEntity<Map<String, String>> getCircuitBreakerStatus() {
        CircuitBreaker.State state = cacheAvalancheService.getCircuitBreakerState();
        String description = switch (state) {
            case CLOSED    -> "Normal operation. All requests to DB are allowed.";
            case OPEN      -> "DB is protected! All requests rejected. Waiting for recovery...";
            case HALF_OPEN -> "Testing recovery. Limited requests allowed to probe DB health.";
            default        -> "Unknown state.";
        };
        return ResponseEntity.ok(Map.of("state", state.name(), "description", description));
    }

    @PostMapping("/api/cache/warm-up")
    public ResponseEntity<Map<String, Object>> manualWarmUp() {
        int warmedCount = cacheWarmingService.manualWarmUp();
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "warmedKeys", warmedCount,
                "message", "Cache warmed up successfully. System ready for traffic."
        ));
    }

    // =========================================================================
    // Problem 03 - Cache Breakdown APIs
    // =========================================================================

    /**
     * Solution 1: Lấy hotspot data KHÔNG có TTL.
     *
     * Thử nghiệm:
     *   1. Gọi lần 1 → [NO-TTL][CACHE MISS] → query DB → set cache (no expiry)
     *   2. Gọi lần 2 → [NO-TTL][CACHE HIT]  → trả về ngay
     *   3. Kiểm tra Redis: redis-cli TTL user:1 → "-1" (không có TTL)
     *
     * Curl: GET http://localhost:8080/api/users/1/hotspot-no-ttl
     */
    @GetMapping("/api/users/{id}/hotspot-no-ttl")
    public ResponseEntity<UserProfile> getHotspotNoTtl(@PathVariable Long id) {
        return cacheBreakdownService.getHotspotWithNoTtl(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Solution 3: Lấy hotspot data với Mutex Lock (Redis SETNX).
     *
     * Cơ chế quan sát tốt nhất: Dùng tool như Apache JMeter hoặc k6 gửi
     * 100 request đồng thời cho cùng 1 userId (khi cache đang trống).
     *
     * Log mong đợi:
     *   [MUTEX][LOCK ACQUIRED] userId=1 → I am the ONE querying DB!
     *   [MUTEX][CACHE POPULATED] userId=1 → cache is now hot!
     *   [MUTEX][LOCK RELEASED] userId=1
     *   [MUTEX][WAITING] userId=1, attempt=1/5    (×99 request còn lại)
     *   [MUTEX][CACHE HIT after wait] userId=1, attempt=1
     *   [MUTEX][CACHE HIT after wait] userId=1, attempt=1
     *   ...
     *
     * Curl: GET http://localhost:8080/api/users/1/hotspot-mutex
     * (Để thấy hiệu quả, xóa cache trước: redis-cli DEL user:1)
     */
    @GetMapping("/api/users/{id}/hotspot-mutex")
    public ResponseEntity<UserProfile> getHotspotMutex(@PathVariable Long id) {
        return cacheBreakdownService.getHotspotWithMutex(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(503).build()); // 503 nếu mutex timeout
    }

    /**
     * Xem TTL còn lại (giây) của một user cache key.
     * Dùng để theo dõi Background Job (Solution 2) có đang refresh đúng không.
     *
     * Giá trị trả về:
     *   > 0:  Số giây còn lại
     *   -1:   Key tồn tại nhưng không có TTL (Solution 1 - No TTL)
     *   -2:   Key không tồn tại trong cache
     *
     * Thử nghiệm Solution 2 (Background Job):
     *   1. Gọi GET /api/users/1/hotspot-no-ttl để populate cache (hoặc đợi warming)
     *   2. Gọi GET /api/cache/ttl/1 → thấy TTL đang đếm ngược
     *   3. Đợi Background Job chạy (mỗi 45s) → gọi lại → TTL được reset về 60s
     *
     * Curl: GET http://localhost:8080/api/cache/ttl/1
     */
    @GetMapping("/api/cache/ttl/{userId}")
    public ResponseEntity<Map<String, Object>> getCacheTtl(@PathVariable Long userId) {
        long ttlSeconds = cacheBreakdownService.getRemainingTtl(userId);

        String status = switch ((int) ttlSeconds) {
            case -2 -> "KEY NOT IN CACHE";
            case -1 -> "KEY EXISTS (No TTL — Solution 1: No expiration set)";
            default -> "KEY EXISTS — expires in " + ttlSeconds + "s";
        };

        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "cacheKey", "user:" + userId,
                "ttlSeconds", ttlSeconds,
                "status", status
        ));
    }

    // ==========================================
    // PROBLEM 04 - CACHE PENETRATION ENDPOINTS
    // ==========================================
    
    private final com.tamdao.caching_design.service.CachePenetrationService cachePenetrationService;

    /**
     * Cách cũ (Legacy) - Chưa có cơ chế chống Cache Penetration.
     * Khi gọi liên tục với ID ma (ví dụ: 9999), cache luôn bị MISS và đi thẳng xuống DB.
     *
     * Curl: GET http://localhost:8080/api/users/9999/penetration-legacy
     */
    @GetMapping("/api/users/{id}/penetration-legacy")
    public ResponseEntity<UserProfile> getPenetrationLegacy(@PathVariable Long id) {
        UserProfile user = cachePenetrationService.getUserLegacy(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    /**
     * Solution 1 + 2: Validate Request + Cache Null Value.
     * - Báo lỗi lập tức nếu ID <= 0 (Validate).
     * - Nếu ID hợp lệ nhưng không tồn tại trong DB, lưu NULL_SENTINEL vào cache với TTL ngắn (2p)
     *   để các request sau nhận Cache HIT ngay mà không làm phiền DB.
     *
     * Curl: GET http://localhost:8080/api/users/9999/penetration-cache-null
     */
    @GetMapping("/api/users/{id}/penetration-cache-null")
    public ResponseEntity<UserProfile> getPenetrationCacheNull(@PathVariable Long id) {
        try {
            UserProfile user = cachePenetrationService.getUserWithCacheNull(id);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Solution 3: Bloom Filter.
     * - Sử dụng Bloom Filter kiểm tra siêu tốc xem ID có tồn tại trong hệ thống hay không.
     * - Nếu Bloom Filter báo không có (chắc chắn 100%), chặn ngay lập tức, không kiểm tra Redis/DB.
     *
     * Curl: GET http://localhost:8080/api/users/9999/penetration-bloom
     */
    @GetMapping("/api/users/{id}/penetration-bloom")
    public ResponseEntity<UserProfile> getPenetrationBloom(@PathVariable Long id) {
        try {
            UserProfile user = cachePenetrationService.getUserWithBloomFilter(id);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ==========================================
    // PROBLEM 05 - HOT KEYS ENDPOINTS
    // ==========================================

    /**
     * Solution 1: Local Cache (JVM RAM).
     * Dữ liệu hot được cache ngay tại RAM JVM để tránh tối đa việc truy vấn qua mạng tới Redis.
     *
     * Curl: GET http://localhost:8080/api/users/{id}/hotkey-local
     */
    @GetMapping("/api/users/{id}/hotkey-local")
    public ResponseEntity<UserProfile> getHotkeyLocal(@PathVariable Long id) {
        UserProfile user = cacheHotKeyService.getUserWithLocalCache(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    /**
     * Solution 2: Key Replication / Sharding (Nhân bản key).
     * Nhân bản key ra nhiều node khác nhau để giảm tải tập trung cho 1 node duy nhất trong Redis Cluster.
     *
     * Curl: GET http://localhost:8080/api/users/{id}/hotkey-replicated
     */
    @GetMapping("/api/users/{id}/hotkey-replicated")
    public ResponseEntity<UserProfile> getHotkeyReplicated(@PathVariable Long id) {
        UserProfile user = cacheHotKeyService.getUserWithReplicatedKey(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    // ==========================================
    // PROBLEM 06 - LARGE KEY ENDPOINTS
    // ==========================================

    /**
     * Kiểm thử Solution 1: Ghi dữ liệu lớn dạng Nén GZIP lên Redis
     *
     * Curl: POST http://localhost:8080/api/cache/large-key/compress/{key}
     */
    @PostMapping("/api/cache/large-key/compress/{key}")
    public ResponseEntity<Map<String, Object>> saveCompressed(@PathVariable String key) {
        var data = cacheLargeKeyService.generateLargeDataset();
        cacheLargeKeyService.saveLargeDataCompressed(key, data);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Đã nén và lưu bộ dữ liệu 50,000 items thành công.",
                "originalSize", data.size()
        ));
    }

    /**
     * Kiểm thử Solution 1: Đọc dữ liệu lớn và giải nén từ Redis
     *
     * Curl: GET http://localhost:8080/api/cache/large-key/compress/{key}
     */
    @GetMapping("/api/cache/large-key/compress/{key}")
    public ResponseEntity<List<Map<String, Object>>> getCompressed(@PathVariable String key) {
        var data = cacheLargeKeyService.getLargeDataCompressed(key);
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(data);
    }

    /**
     * Kiểm thử Solution 2: Ghi dữ liệu lớn bằng cách Chia nhỏ (Splitting) thành các chunk song song
     *
     * Curl: POST http://localhost:8080/api/cache/large-key/split/{key}
     */
    @PostMapping("/api/cache/large-key/split/{key}")
    public ResponseEntity<Map<String, Object>> saveSplit(@PathVariable String key) {
        var data = cacheLargeKeyService.generateLargeDataset();
        cacheLargeKeyService.saveLargeDataSplit(key, data);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Đã chia nhỏ dữ liệu thành các chunks 10,000 items và ghi song song lên Redis.",
                "originalSize", data.size()
        ));
    }

    /**
     * Kiểm thử Solution 2: Đọc dữ liệu chia nhỏ song song và gộp lại từ Redis
     *
     * Curl: GET http://localhost:8080/api/cache/large-key/split/{key}
     */
    @GetMapping("/api/cache/large-key/split/{key}")
    public ResponseEntity<List<Map<String, Object>>> getSplit(@PathVariable String key) {
        var data = cacheLargeKeyService.getLargeDataSplit(key);
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(data);
    }
}
