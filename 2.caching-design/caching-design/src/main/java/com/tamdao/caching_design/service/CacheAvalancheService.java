package com.tamdao.caching_design.service;

import com.tamdao.caching_design.model.UserProfile;
import com.tamdao.caching_design.repository.UserProfileRepository;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * =============================================================================
 *  CacheAvalancheService
 *  Giải pháp cho: Problem 02 - Cache Avalanche (README section 3.1.2)
 * =============================================================================
 *
 *  PROBLEM: Hai nguyên nhân gây "Tuyết lở Cache":
 *
 *  NGUYÊN NHÂN 1 — Cache Sập đột ngột (Cache Down):
 *    Toàn bộ Redis cluster bị down → 100% request đổ thẳng xuống DB
 *    → DB không chịu được tải → DB sập theo → toàn hệ thống tê liệt
 *
 *  NGUYÊN NHÂN 2 — Đáo hạn diện rộng (Mass TTL Expiry):
 *    Nếu ta cache 10,000 sản phẩm cùng lúc với TTL = 5 phút:
 *    → Sau đúng 5 phút, TẤT CẢ 10,000 key đồng loạt hết hạn
 *    → 10,000 Cache Miss đồng thời → 10,000 query song song xuống DB
 *    → DB bị quá tải (dù Redis vẫn sống)
 *
 *  ┌─────────────────────────────────────────────────────────────────────┐
 *  │  GIẢI PHÁP TRONG CLASS NÀY                                          │
 *  │                                                                     │
 *  │  Solution 2: Circuit Breaker (cho nguyên nhân Cache Sập)           │
 *  │  Solution 4: TTL Jitter (cho nguyên nhân Đáo hạn diện rộng)        │
 *  └─────────────────────────────────────────────────────────────────────┘
 *
 *  Solution 1 (Cache Cluster HA): Không demo được trong code — là cấu hình
 *  infrastructure (Redis Sentinel hoặc Redis Cluster với nhiều node).
 *
 *  Solution 3 (Cache Warming): Xem CacheWarmingService.java
 */
@Service
@Slf4j
public class CacheAvalancheService {

    private final UserProfileRepository userProfileRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * CircuitBreaker cho tầng DB — trái tim của Solution 2.
     *
     * Circuit Breaker là một "cầu dao điện" tự động:
     *
     *   CLOSED (Bình thường):
     *     → Cho phép tất cả request qua DB
     *     → Theo dõi tỉ lệ lỗi (error rate)
     *
     *   OPEN (Ngắt mạch — DB bị bảo vệ):
     *     → Xảy ra khi error rate vượt ngưỡng (ví dụ: 50% request thất bại)
     *     → TẤT CẢ request bị từ chối ngay lập tức (throw CallNotPermittedException)
     *     → KHÔNG một request nào được chui xuống DB
     *     → Cho DB có thời gian hồi phục (wait-duration-in-open-state = 10s)
     *
     *   HALF-OPEN (Thăm dò):
     *     → Sau thời gian chờ, thử cho 1 số request qua DB
     *     → Nếu thành công → chuyển về CLOSED
     *     → Nếu vẫn lỗi → quay lại OPEN
     *
     *  Timeline khi Cache sập và DB quá tải:
     *
     *    t=0s:  Cache down, 1000 req/s đổ xuống DB
     *    t=2s:  DB bắt đầu timeout (error rate = 60% > 50%)
     *    t=2s:  ↳ Circuit OPENS — DB được bảo vệ
     *    t=2s~12s: Tất cả request nhận lỗi ngay từ CB, DB không bị chạm
     *    t=12s: Circuit HALF-OPEN — thử 3 request
     *    t=12s: DB đã hồi phục → 3 request thành công
     *    t=12s: ↳ Circuit CLOSES — hệ thống hoạt động bình thường lại
     *
     *  So sánh:
     *    Không có CB: DB chết, toàn bộ hệ thống tê liệt (vô thời hạn)
     *    Có CB:       DB được bảo vệ, phục hồi trong ~10 giây
     */
    private final CircuitBreaker dbCircuitBreaker;

    // =========================================================================
    // SOLUTION 4 Constants: TTL Base + Jitter Range
    // =========================================================================

    /**
     * TTL cơ bản (Base TTL): 5 phút.
     * Mỗi key sẽ sống ÍT NHẤT 5 phút.
     */
    private static final Duration BASE_TTL = Duration.ofMinutes(5);

    /**
     * Khoảng Jitter tối đa: 2 phút.
     * Mỗi key sẽ được cộng thêm một khoảng ngẫu nhiên từ 0 → 120 giây.
     *
     * Kết quả thực tế:
     *   Key A: TTL = 5 phút + 47s  = 5m47s
     *   Key B: TTL = 5 phút + 112s = 6m52s
     *   Key C: TTL = 5 phút + 8s   = 5m08s
     *   → Ba key KHÔNG BAO GIỜ hết hạn cùng một lúc!
     */
    private static final Duration MAX_JITTER = Duration.ofMinutes(2);

    private static final String CACHE_KEY_PREFIX = "user:";

    public CacheAvalancheService(UserProfileRepository userProfileRepository,
                                 RedisTemplate<String, Object> redisTemplate) {
        this.userProfileRepository = userProfileRepository;
        this.redisTemplate = redisTemplate;
        this.dbCircuitBreaker = buildCircuitBreaker();
    }

    /**
     * Khởi tạo CircuitBreaker với cấu hình tùy chỉnh.
     *
     * Thay vì dùng Spring Boot autoconfigure (cần YAML phức tạp),
     * ta dùng programmatic API của Resilience4j — rõ ràng và dễ học hơn.
     */
    private CircuitBreaker buildCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                // Kiểu sliding window: COUNT_BASED = đếm N request gần nhất
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)

                // Xem xét 10 request gần nhất để tính error rate
                .slidingWindowSize(10)

                // Ngưỡng lỗi: 50% → nếu 5/10 request thất bại → OPEN
                .failureRateThreshold(50)

                // Sau khi OPEN, chờ 10 giây trước khi thử HALF-OPEN
                .waitDurationInOpenState(Duration.ofSeconds(10))

                // Số request cho phép khi HALF-OPEN để kiểm tra
                .permittedNumberOfCallsInHalfOpenState(3)

                // Các exception nào bị tính là "lỗi" để trigger OPEN
                .recordExceptions(Exception.class)
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        CircuitBreaker cb = registry.circuitBreaker("db-protection");

        // Log khi trạng thái Circuit Breaker thay đổi
        cb.getEventPublisher().onStateTransition(event ->
            log.warn("[CIRCUIT BREAKER] State changed: {} → {} (userId context)",
                    event.getStateTransition().getFromState(),
                    event.getStateTransition().getToState())
        );

        return cb;
    }

    // =========================================================================
    // SOLUTION 2: Circuit Breaker + SOLUTION 4: TTL Jitter (kết hợp)
    // =========================================================================

    /**
     * Lấy UserProfile với đầy đủ bảo vệ chống Cache Avalanche:
     *   - Solution 2: Circuit Breaker bảo vệ DB khi cache sập
     *   - Solution 4: TTL với Jitter ngẫu nhiên tránh đáo hạn diện rộng
     *
     * API endpoint: GET /api/users/{id}/safe
     *
     * @param userId ID của user cần lấy
     * @return Optional<UserProfile>
     */
    public Optional<UserProfile> getUserWithAvalancheProtection(Long userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;

        // ─── BƯỚC 1: Kiểm tra Cache trước ─────────────────────────────────
        // Nếu cache còn sống → hit cache, không cần CB hay DB
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("[SAFE][CACHE HIT] userId={}", userId);
            return Optional.of((UserProfile) cached);
        }

        log.debug("[SAFE][CACHE MISS] userId={} → querying DB with Circuit Breaker", userId);

        // ─── BƯỚC 2: Wrap DB query bằng Circuit Breaker ───────────────────
        //
        // CircuitBreaker.decorateSupplier():
        //   Bọc đoạn code "xuống DB" vào một Supplier được CB giám sát.
        //   - Nếu CB CLOSED → chạy bình thường
        //   - Nếu CB OPEN → throw CallNotPermittedException ngay lập tức
        //                    (không chạm đến DB)
        //   - Nếu CB HALF-OPEN → chạy nhưng theo dõi kết quả để quyết định
        //                         CLOSE hay OPEN lại
        Supplier<Optional<UserProfile>> dbQuery = CircuitBreaker.decorateSupplier(
                dbCircuitBreaker,
                () -> userProfileRepository.findById(userId)
        );

        try {
            Optional<UserProfile> userFromDb = dbQuery.get();

            // ─── BƯỚC 3: Write-Back với JITTER TTL (Solution 4) ───────────
            //
            // Điểm khác biệt quan trọng so với getUserProfile() thông thường:
            //   Thông thường: TTL = 5 phút (cố định cho TẤT CẢ key)
            //   Với Jitter:   TTL = random(5m, 7m) → mỗi key có TTL khác nhau
            //
            // Kết quả thực tế khi cache 1000 users trong vòng 1 giây:
            //   Không jitter: Sau đúng 5 phút → 1000 Cache Miss đồng thời ❌
            //   Có jitter:    Keys hết hạn rải đều từ 5m đến 7m            ✅
            //                  → tối đa ~8 key/giây hết hạn thay vì 1000 key/giây
            userFromDb.ifPresent(user -> {
                Duration jitteredTtl = getTtlWithJitter(BASE_TTL, MAX_JITTER);
                redisTemplate.opsForValue().set(cacheKey, user, jitteredTtl);
                log.debug("[SAFE][CACHE WRITE-BACK] userId={}, TTL={}s (base={}s + jitter)",
                        userId, jitteredTtl.toSeconds(), BASE_TTL.toSeconds());
            });

            return userFromDb;

        } catch (CallNotPermittedException e) {
            // ─── CIRCUIT BREAKER OPEN: DB đang được bảo vệ ───────────────
            //
            // Điều này xảy ra khi:
            //   1. Cache đang down (Cache Avalanche nguyên nhân 1)
            //   2. DB đã bắt đầu quá tải (error rate ≥ 50%)
            //   3. Circuit Breaker đã OPEN để ngăn thêm request xuống DB
            //
            // Tại đây, hệ thống có thể:
            //   - Trả về lỗi 503 Service Unavailable
            //   - Trả về dữ liệu cũ từ local cache / stale cache
            //   - Trả về dữ liệu mặc định (degraded response)
            //
            // Ở đây ta trả về empty → controller sẽ return 503
            log.warn("[SAFE][CIRCUIT OPEN] DB is protected for userId={}. " +
                     "Circuit state: {}. Returning empty to avoid DB overload.",
                     userId, dbCircuitBreaker.getState());
            return Optional.empty();
        }
    }

    /**
     * Lấy trạng thái hiện tại của Circuit Breaker — dùng cho monitoring.
     * API endpoint: GET /api/cache/circuit-breaker/status
     *
     * Trạng thái có thể là:
     *   CLOSED:    Bình thường, mọi request qua DB
     *   OPEN:      Đang ngắt mạch, DB được bảo vệ
     *   HALF_OPEN: Đang thăm dò để kiểm tra DB đã hồi phục chưa
     */
    public CircuitBreaker.State getCircuitBreakerState() {
        return dbCircuitBreaker.getState();
    }

    // =========================================================================
    // SOLUTION 4: TTL Jitter Utility
    // =========================================================================

    /**
     * Tính TTL có kèm Jitter ngẫu nhiên.
     *
     * Công thức:
     *   actualTtl = baseTtl + random(0, maxJitter)
     *
     * Ví dụ với BASE_TTL=5min, MAX_JITTER=2min:
     *   User 1:  5m + 0m23s = 5m23s
     *   User 2:  5m + 1m47s = 6m47s
     *   User 3:  5m + 0m03s = 5m03s
     *   → Các key hết hạn RẢI ĐỀU trong khoảng [5m, 7m]
     *   → Không bao giờ đồng loạt hết hạn một lúc
     *
     * Tại sao KHÔNG dùng TTL cố định?
     *
     *   Kịch bản: Startup hệ thống lúc 8:00:00, cache 1000 products đồng thời
     *   TTL cố định = 30 phút → tất cả hết hạn lúc 8:30:00 (đồng loạt!)
     *   TTL jitter   = 30-35min → hết hạn rải đều từ 8:30 đến 8:35
     *                              → chỉ ~3 key/giây hết hạn thay vì 1000 key/giây
     *
     * @param baseTtl  TTL cơ bản (tối thiểu)
     * @param maxJitter Giới hạn tối đa của khoảng ngẫu nhiên cộng thêm
     * @return Duration = baseTtl + random(0, maxJitter)
     */
    public Duration getTtlWithJitter(Duration baseTtl, Duration maxJitter) {
        // ThreadLocalRandom: thread-safe, hiệu năng cao hơn Random trong môi trường concurrent
        long jitterSeconds = ThreadLocalRandom.current().nextLong(0, maxJitter.toSeconds());
        Duration result = baseTtl.plusSeconds(jitterSeconds);
        log.debug("[TTL JITTER] base={}s + jitter={}s = total={}s",
                baseTtl.toSeconds(), jitterSeconds, result.toSeconds());
        return result;
    }
}
