package com.tamdao.caching_design.service;

import com.tamdao.caching_design.model.UserProfile;
import com.tamdao.caching_design.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * =============================================================================
 *  CacheBreakdownService
 *  Giải pháp cho: Problem 03 - Cache Breakdown (README section 3.1.3)
 * =============================================================================
 *
 *  PROBLEM: "Thủng Cache do Hotspot"
 *
 *  Cache Avalanche (Problem 02) = Hàng NGÀN KEY đồng loạt hết hạn
 *  Cache Breakdown (Problem 03) = CHỈ MỘT KEY nhưng cực kỳ "nóng"
 *
 *  Tình huống:
 *    - Key "user:999" = tài khoản idol có 10 triệu followers
 *    - Mỗi giây có 50,000 request hỏi "user:999"
 *    - Đúng lúc TTL của key này hết → 50,000 request thấy Cache Miss
 *    - 50,000 request đồng loạt query DB → DB sập
 *
 *  ┌────────────────────────────────────────────────────────────────────┐
 *  │  BA GIẢI PHÁP TRONG CLASS NÀY                                      │
 *  │                                                                    │
 *  │  Solution 1: Không set TTL — cache tồn tại vĩnh viễn              │
 *  │  Solution 2: Background Job — refresh cache trước khi hết hạn     │
 *  │  Solution 3: Mutex Lock — chỉ 1 request xuống DB, còn lại chờ     │
 *  └────────────────────────────────────────────────────────────────────┘
 *
 *  Endpoints để test:
 *    GET /api/users/{id}/hotspot-no-ttl    → Solution 1
 *    GET /api/users/{id}/hotspot-mutex     → Solution 3
 *    (Solution 2 chạy tự động - không cần gọi API)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheBreakdownService {

    private final UserProfileRepository userProfileRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "user:";

    /**
     * Danh sách ID của các hotspot user cần được Background Job refresh.
     *
     * Production thực tế:
     *   - Đây có thể là danh sách động, được cập nhật từ analytics
     *   - Ví dụ: top 100 user được truy cập nhiều nhất trong 1 giờ qua
     *   - Demo: hardcode 3 user đầu tiên làm "hotspot"
     */
    private static final Set<Long> HOTSPOT_USER_IDS = Set.of(1L, 2L, 3L);

    /** TTL ngắn cho hotspot (chỉ 1 phút) để dễ quan sát demo */
    private static final Duration HOTSPOT_TTL = Duration.ofMinutes(1);

    // =========================================================================
    // SOLUTION 1: Không set TTL (Do not set expiration time)
    // =========================================================================

    /**
     * Lấy hotspot data mà KHÔNG set TTL — key tồn tại vĩnh viễn trong Redis.
     *
     * GIẢI THÍCH GIẢI PHÁP:
     *
     *   Nếu TTL = vô hạn → key KHÔNG BAO GIỜ hết hạn → Cache Miss KHÔNG BAO GIỜ xảy ra
     *   → Cache Breakdown không thể xảy ra (không có hết hạn = không có "thủng")
     *
     * ĐÁNH ĐỔI (Trade-offs):
     *
     *   ✅ Ưu điểm:
     *      - Đơn giản nhất để implement
     *      - Cache Hit Rate = 100% (sau lần đầu)
     *      - DB không bao giờ bị "thủng"
     *
     *   ❌ Nhược điểm:
     *      - Nếu DB update dữ liệu → Cache sẽ bị STALE vĩnh viễn!
     *      - Phải có cơ chế invalidate thủ công khi data thay đổi
     *      - Tốn RAM: key không bao giờ tự giải phóng
     *
     * KHI NÀO NÊN DÙNG:
     *   - Dữ liệu KHÔNG BAO GIỜ thay đổi (config hệ thống, lookup table)
     *   - Dữ liệu real-time được update liên tục bởi background job (Solution 2)
     *     → Không cần TTL vì background job sẽ refresh
     *   - Ví dụ thực tế: tỷ giá tiền tệ, cấu hình feature flag, thông tin user VIP
     *
     * @param userId ID của hotspot user
     * @return Optional<UserProfile>
     */
    public Optional<UserProfile> getHotspotWithNoTtl(Long userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;

        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("[NO-TTL][CACHE HIT] userId={}", userId);
            return Optional.of((UserProfile) cached);
        }

        log.debug("[NO-TTL][CACHE MISS] userId={} → query DB (sẽ không xảy ra lần 2!)", userId);

        Optional<UserProfile> userFromDb = userProfileRepository.findById(userId);

        userFromDb.ifPresent(user -> {
            // ← CHỖ KHÁC BIỆT: Không truyền Duration → key không có TTL
            // Redis lưu mãi mãi cho đến khi bị xóa thủ công hoặc Redis restart
            redisTemplate.opsForValue().set(cacheKey, user);
            log.debug("[NO-TTL][CACHE SET] userId={}, key={} ← NO EXPIRATION", userId, cacheKey);
        });

        return userFromDb;
    }

    // =========================================================================
    // SOLUTION 2: Background Job — refresh cache TRƯỚC KHI hết hạn
    // =========================================================================

    /**
     * Background Job tự động chạy mỗi 45 giây để refresh hotspot cache.
     *
     * GIẢI THÍCH GIẢI PHÁP:
     *
     *   Thay vì chờ cache hết hạn (Cache Miss) mới query DB → Chủ động refresh
     *   cache TRƯỚC KHI nó hết hạn → Cache KHÔNG BAO GIỜ bị "trống" với hotspot data.
     *
     *   Timeline minh họa (HOTSPOT_TTL = 1 phút, job chạy mỗi 45s):
     *
     *   t=0:00  Key "user:1" được set, TTL = 60s
     *   t=0:45  Background Job chạy → refresh key, TTL reset về 60s
     *   t=1:00  Key CẼ hết hạn, nhưng...
     *   t=1:00  ...nó đã được refresh rồi! TTL vẫn còn ~45s
     *   t=1:30  Background Job chạy lại → refresh, TTL reset về 60s
     *   → Key KHÔNG BAO GIỜ hết hạn khi có request đang đến!
     *
     * @Scheduled parameters:
     *
     * - fixedDelay = 45000 (45 giây):
     *   Chờ 45 giây SAU KHI lần chạy trước kết thúc rồi mới chạy lại.
     *   (Khác với fixedRate: chạy mỗi N ms kể từ lúc bắt đầu lần trước)
     *   → fixedDelay an toàn hơn: tránh job chồng chéo nếu job chạy lâu
     *
     * - initialDelay = 10000 (10 giây):
     *   Chờ 10 giây sau khi app start rồi mới chạy lần đầu.
     *   → Tránh conflict với DataInitializer và CacheWarmingService đang chạy
     *
     * ĐÁNH ĐỔI (Trade-offs):
     *
     *   ✅ Ưu điểm:
     *      - Cache luôn "nóng", không bao giờ bị Breakdown
     *      - Dữ liệu cache luôn tươi (refresh từ DB định kỳ)
     *      - Không cần thay đổi code luồng đọc
     *
     *   ❌ Nhược điểm:
     *      - Tốn tài nguyên: liên tục query DB dù không ai request
     *      - Phải maintain danh sách hotspot keys
     *      - Nếu job bị trễ → vẫn có window nguy hiểm nhỏ
     *
     * KHI NÀO NÊN DÙNG:
     *   - Hotspot data đã biết trước (top sellers, trending items, config)
     *   - Data thay đổi thường xuyên nhưng không critical (tỷ số bóng đá)
     */
    @Scheduled(fixedDelay = 45000, initialDelay = 10000)
    public void refreshHotspotCache() {
        log.debug("[BG JOB] Starting hotspot cache refresh for {} users...", HOTSPOT_USER_IDS.size());

        for (Long userId : HOTSPOT_USER_IDS) {
            try {
                String cacheKey = CACHE_KEY_PREFIX + userId;

                // Query DB lấy dữ liệu mới nhất
                Optional<UserProfile> freshData = userProfileRepository.findById(userId);

                freshData.ifPresent(user -> {
                    // Ghi đè lên cache với TTL mới → TTL được "reset" về HOTSPOT_TTL
                    redisTemplate.opsForValue().set(cacheKey, user, HOTSPOT_TTL);
                    log.debug("[BG JOB] Refreshed hotspot cache: userId={}, TTL={}s",
                            userId, HOTSPOT_TTL.toSeconds());
                });

                if (freshData.isEmpty()) {
                    log.warn("[BG JOB] User {} not found in DB — removing from hotspot list?", userId);
                }

            } catch (Exception e) {
                // KHÔNG để 1 user lỗi làm dừng job cho các user khác
                log.error("[BG JOB] Failed to refresh cache for userId={}: {}", userId, e.getMessage());
            }
        }

        log.debug("[BG JOB] Hotspot refresh complete.");
    }

    // =========================================================================
    // SOLUTION 3: Mutex Lock (Redis SETNX) — chỉ 1 thread xuống DB
    // =========================================================================

    /**
     * Lấy hotspot data với Mutex Lock — ngăn Cache Stampede hiệu quả nhất.
     *
     * GIẢI THÍCH GIẢI PHÁP:
     *
     *   "Cache Stampede" = Hàng vạn request cùng thấy Cache Miss và đổ xuống DB.
     *
     *   Mutex (Mutual Exclusion Lock) giải quyết bằng cách:
     *     - Chỉ cho phép MỘT request duy nhất "thắng" được lock và xuống DB
     *     - Tất cả request khác phải CHỜ cho đến khi request thắng
     *       hoàn thành và ghi dữ liệu lên cache
     *     - Request chờ sau đó đọc từ cache → Cache Hit
     *
     *   Cơ chế Redis SETNX (SET if Not eXists):
     *
     *   ┌──────────────────────────────────────────────────────────────────┐
     *   │  Redis SETNX là gì?                                              │
     *   │                                                                  │
     *   │  SET "lock:user:1" "locked" NX PX 3000                          │
     *   │        ↑ Key        ↑ Value  ↑↑ Chỉ set NẾU chưa tồn tại       │
     *   │                                   ↑ TTL = 3000ms                │
     *   │                                                                  │
     *   │  Trả về TRUE:  Key chưa tồn tại → Thành công acquire lock       │
     *   │  Trả về FALSE: Key đã tồn tại  → Thread khác đang giữ lock      │
     *   │                                                                  │
     *   │  ATOMIC: SETNX là atomic trong Redis (single-threaded)          │
     *   │  → Dù 10,000 thread cùng gọi, CHỈ 1 thread nhận TRUE           │
     *   └──────────────────────────────────────────────────────────────────┘
     *
     *   Luồng thực thi với 3 thread đồng thời:
     *
     *     [Thread A] SETNX lock:user:1 → TRUE  → xuống DB → ghi cache → xóa lock
     *     [Thread B] SETNX lock:user:1 → FALSE → wait 50ms → đọc cache → HIT ✅
     *     [Thread C] SETNX lock:user:1 → FALSE → wait 50ms → đọc cache → HIT ✅
     *
     *   Kết quả: DB chỉ bị query 1 lần thay vì 10,000 lần!
     *
     * ĐÁNH ĐỔI (Trade-offs):
     *
     *   ✅ Ưu điểm:
     *      - Hiệu quả nhất: DB chỉ chịu 1 query dù có N thread đồng thời
     *      - Tự động: không cần maintain danh sách hotspot
     *      - Works cho BẤT KỲ key nào (không cần biết trước là hotspot)
     *
     *   ❌ Nhược điểm:
     *      - Phức tạp hơn (cần xử lý timeout, deadlock)
     *      - Thread phải chờ → tăng latency cho các request không thắng lock
     *      - Lock TTL phải được cài đặt cẩn thận (quá ngắn → deadlock; quá dài → chờ lâu)
     *
     * @param userId ID của hotspot user
     * @return Optional<UserProfile>
     */
    public Optional<UserProfile> getHotspotWithMutex(Long userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;
        String lockKey = "lock:" + cacheKey;   // VD: "lock:user:1"

        // ─── BƯỚC 1: Kiểm tra cache ──────────────────────────────────────
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("[MUTEX][CACHE HIT] userId={}", userId);
            return Optional.of((UserProfile) cached);
        }

        // ─── BƯỚC 2: Cache Miss → Tranh giành Mutex Lock ─────────────────
        log.debug("[MUTEX][CACHE MISS] userId={} → attempting to acquire lock", userId);

        // setIfAbsent() = Redis SETNX (SET if Not eXists):
        //   - Trả về TRUE: Thread này thắng lock → được xuống DB
        //   - Trả về FALSE: Thread khác đang giữ lock → phải chờ
        //
        // Lock TTL = 3 giây: Safety net tránh deadlock
        //   Nếu thread thắng bị crash sau khi lock nhưng trước khi xóa lock,
        //   lock sẽ tự động hết hạn sau 3 giây (tránh hệ thống bị treo mãi)
        Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "locked", Duration.ofSeconds(3));

        if (Boolean.TRUE.equals(lockAcquired)) {
            // ─── THREAD THẮNG LOCK → Xuống DB ────────────────────────────
            log.debug("[MUTEX][LOCK ACQUIRED] userId={} → I am the ONE querying DB!", userId);

            try {
                Optional<UserProfile> userFromDb = userProfileRepository.findById(userId);

                // Ghi kết quả lên cache để các thread khác có thể đọc
                userFromDb.ifPresent(user -> {
                    redisTemplate.opsForValue().set(cacheKey, user, HOTSPOT_TTL);
                    log.debug("[MUTEX][CACHE POPULATED] userId={} → cache is now hot!", userId);
                });

                return userFromDb;

            } finally {
                // BẮT BUỘC phải xóa lock trong finally để tránh deadlock
                // Dù DB query thành công hay thất bại → lock phải được giải phóng
                redisTemplate.delete(lockKey);
                log.debug("[MUTEX][LOCK RELEASED] userId={}", userId);
            }

        } else {
            // ─── THREAD THUA LOCK → Chờ và đọc lại cache ────────────────
            //
            // Chiến lược: Chờ 100ms rồi thử đọc cache lại.
            // Thread thắng lock đang query DB + ghi cache → cần thời gian.
            //
            // Vòng lặp retry tối đa 5 lần × 100ms = 500ms chờ tối đa.
            // Nếu sau 500ms vẫn không có cache → trả về empty (timeout).
            log.debug("[MUTEX][LOCK NOT ACQUIRED] userId={} → waiting for cache population...", userId);

            int maxRetries = 5;
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    // Chờ 100ms để thread thắng lock kịp populate cache
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return Optional.empty();
                }

                // Thử đọc cache lại
                Object retryCache = redisTemplate.opsForValue().get(cacheKey);
                if (retryCache != null) {
                    log.debug("[MUTEX][CACHE HIT after wait] userId={}, attempt={}", userId, attempt);
                    return Optional.of((UserProfile) retryCache);
                }

                log.debug("[MUTEX][WAITING] userId={}, attempt={}/{}", userId, attempt, maxRetries);
            }

            // Hết retry → trả về empty (request này chịu timeout)
            log.warn("[MUTEX][TIMEOUT] userId={} — could not get data after {}ms wait",
                    userId, maxRetries * 100);
            return Optional.empty();
        }
    }

    /**
     * Demo: Xem TTL còn lại của hotspot key (giây).
     * Dùng để theo dõi Background Job có refresh đúng không.
     *
     * API endpoint: GET /api/cache/ttl/{userId}
     */
    public long getRemainingTtl(Long userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;
        Long ttlMs = redisTemplate.getExpire(cacheKey, TimeUnit.SECONDS);
        return ttlMs != null ? ttlMs : -2; // -2 = key không tồn tại, -1 = key không có TTL
    }
}
