package com.tamdao.caching_design.service;

import com.tamdao.caching_design.model.UserProfile;
import com.tamdao.caching_design.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * =============================================================================
 *  CacheWarmingService
 *  Giải pháp cho: Problem 02 - Cache Avalanche (README section 3.1.2)
 *  Solution 3: Cache Recovery — Làm nóng Cache (Cache Warming)
 * =============================================================================
 *
 *  VẤN ĐỀ mà Cache Warming giải quyết:
 *
 *  Kịch bản xảy ra Cache Avalanche lúc khởi động (Cold Start):
 *    t=0:  Hệ thống restart (deploy mới / crash recovery)
 *    t=0:  Redis cache hoàn toàn trống rỗng (Cold Cache)
 *    t=0:  Hàng nghìn user đang dùng app, request đổ vào
 *    t=0~?: TẤT CẢ request đều Cache Miss → đổ xuống DB
 *    t=?:  DB không chịu được tải đột ngột → DB down → Avalanche
 *
 *  Cache Warming = Pre-load dữ liệu "nóng" vào Cache TRƯỚC KHI hệ thống
 *  nhận traffic thực → tránh "Cold Start Avalanche"
 *
 *  Chiến lược chọn data để warm:
 *  Nguyên lý 80/20 (Pareto): 20% data phục vụ 80% traffic.
 *  → Chỉ cần warm top 20% hot data là đủ "che" phần lớn request.
 *
 *  Các nguồn để biết data nào "hot":
 *    - Access log từ kỳ trước (replay analytics)
 *    - Business logic biết trước (VD: sản phẩm Flash Sale, user VIP)
 *    - Simple heuristic: load N record mới nhất (demo này dùng cách này)
 *
 *  Khi nào chạy Cache Warming?
 *    - Ngay sau khi app khởi động (Cold Start) ← demo này
 *    - Định kỳ (scheduled job) để refresh hot data
 *    - Trước sự kiện lớn (Flash Sale, Black Friday)
 *    - Sau khi Cache cluster bị sập và phục hồi lại
 *
 *  Lưu ý quan trọng:
 *    Cache Warming cũng nên dùng TTL Jitter để tránh tất cả warmed keys
 *    đồng loạt hết hạn sau đúng N phút! (Solution 3 + Solution 4 phối hợp)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheWarmingService {

    private final UserProfileRepository userProfileRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Số lượng user sẽ được pre-load vào Cache khi khởi động.
     * Production: con số này phụ thuộc vào business (top 1000 user, VIP list, etc.)
     */
    private static final int WARM_UP_LIMIT = 10;

    /**
     * TTL cho warmed keys: dài hơn thông thường vì đây là "hot data"
     * Base = 30 phút, Jitter = 0~5 phút → keys hết hạn rải từ 30m đến 35m
     */
    private static final Duration WARM_TTL_BASE = Duration.ofMinutes(30);
    private static final Duration WARM_TTL_JITTER = Duration.ofMinutes(5);

    private static final String CACHE_KEY_PREFIX = "user:";

    /**
     * Cache Warming chạy ngay sau khi Spring Boot application sẵn sàng nhận request.
     *
     * @EventListener(ApplicationReadyEvent.class):
     *   ApplicationReadyEvent được phát khi:
     *   1. Tất cả beans đã được khởi tạo
     *   2. CommandLineRunner, ApplicationRunner đã chạy xong
     *   3. Embedded server (Tomcat) đã sẵn sàng nhận HTTP request
     *   → ĐÂY LÀ THỜI ĐIỂM ĐÚNG để warm cache, vì:
     *      - DB và Redis đã sẵn sàng (beans inject được rồi)
     *      - Ngay trước khi traffic thực vào (nếu dùng load balancer)
     *
     * TẠI SAO KHÔNG DÙNG @PostConstruct?
     *   @PostConstruct chạy khi bean được tạo, có thể DB/Redis chưa ready.
     *   ApplicationReadyEvent đảm bảo toàn bộ context đã ready.
     *
     * TẠI SAO KHÔNG DÙNG CommandLineRunner (như DataInitializer)?
     *   DataInitializer seed DB → phải chạy trước warming.
     *   ApplicationReadyEvent chạy SAU CommandLineRunner → đúng thứ tự.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmUpCache() {
        log.info("=== [CACHE WARMING] Starting cache warm-up for top {} users ===", WARM_UP_LIMIT);
        long startTime = System.currentTimeMillis();

        try {
            // ─── Load top N hot users từ DB ──────────────────────────────
            // Trong thực tế: query theo access_count, last_accessed, hoặc user tier
            // Demo đơn giản: lấy 10 user đầu tiên (sorted by ID)
            List<UserProfile> hotUsers = userProfileRepository.findAll(
                    PageRequest.of(0, WARM_UP_LIMIT, Sort.by(Sort.Direction.ASC, "id"))
            ).getContent();

            // ─── Pre-load vào Redis với TTL Jitter ───────────────────────
            // QUAN TRỌNG: Dùng Jitter ngay cả khi warming!
            //
            // Nếu KHÔNG dùng jitter khi warming:
            //   - t=0: warm 10 users với TTL = 30 phút
            //   - t=30m: TẤT CẢ 10 key đồng loạt hết hạn → Mini Avalanche!
            //
            // Với Jitter (TTL_BASE=30m, MAX_JITTER=5m):
            //   - User 1: TTL = 30m + 2m13s = 32m13s
            //   - User 2: TTL = 30m + 4m45s = 34m45s
            //   - User 3: TTL = 30m + 0m30s = 30m30s
            //   → Keys hết hạn rải đều trong khoảng [30m, 35m]
            int warmedCount = 0;
            for (UserProfile user : hotUsers) {
                String cacheKey = CACHE_KEY_PREFIX + user.getId();

                // TTL = base + random(0, jitter)
                long jitterSeconds = ThreadLocalRandom.current().nextLong(0, WARM_TTL_JITTER.toSeconds());
                Duration actualTtl = WARM_TTL_BASE.plusSeconds(jitterSeconds);

                redisTemplate.opsForValue().set(cacheKey, user, actualTtl);
                warmedCount++;

                log.debug("[CACHE WARMING] Pre-loaded userId={}, key={}, TTL={}s",
                        user.getId(), cacheKey, actualTtl.toSeconds());
            }

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("=== [CACHE WARMING] Done! Warmed {} users in {}ms. " +
                     "Cache is HOT and ready for traffic. ===", warmedCount, elapsed);

        } catch (Exception e) {
            // QUAN TRỌNG: KHÔNG để lỗi ở đây làm crash toàn bộ app!
            // Cache warming thất bại là acceptable — hệ thống vẫn hoạt động được,
            // chỉ là sẽ có nhiều Cache Miss hơn ở đầu.
            log.error("[CACHE WARMING] Failed to warm cache. System will still work " +
                      "but expect higher DB load initially. Error: {}", e.getMessage());
        }
    }

    /**
     * Manual cache warming — có thể gọi qua API để re-warm cache sau sự cố.
     *
     * Ví dụ use case:
     *   - Redis vừa bị sập và phục hồi → Cache trống → gọi API này để warm lại ngay
     *   - Trước sự kiện Flash Sale → warm trước sản phẩm sẽ bán
     *
     * API endpoint: POST /api/cache/warm-up
     *
     * @return Số lượng key đã được warm
     */
    public int manualWarmUp() {
        log.info("[CACHE WARMING] Manual warm-up triggered");
        warmUpCache();

        // Đếm số key user đang có trong cache (demo đơn giản)
        // Production: dùng Redis SCAN thay vì KEYS để tránh blocking
        return WARM_UP_LIMIT;
    }
}
