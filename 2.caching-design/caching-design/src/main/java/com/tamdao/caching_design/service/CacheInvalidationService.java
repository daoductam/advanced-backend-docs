package com.tamdao.caching_design.service;

import com.tamdao.caching_design.event.UserProfileUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * =============================================================================
 *  CacheInvalidationService
 *  Giải pháp cho: Problem 01 - No Atomicity (README section 3.1.1)
 * =============================================================================
 *
 *  PROBLEM: Trong môi trường phân tán, lệnh xóa Cache có thể THẤT BẠI bất cứ
 *  lúc nào do: mạng chập chờn, Redis timeout, Redis tạm sập...
 *
 *  Hậu quả:
 *    DB  → cập nhật age = 34 ✅
 *    Cache → vẫn còn age = 33 ❌ (không được xóa)
 *    Client đọc → thấy age = 33 (dữ liệu cũ) cho đến khi TTL hết hạn
 *
 *  =============================================================================
 *  SOLUTION 1: Retry (Tự động gọi lại)
 *  =============================================================================
 *
 *  Ý tưởng: Nếu lần xóa cache đầu tiên thất bại, hệ thống tự gọi lại
 *  tối đa N lần trước khi từ bỏ.
 *
 *  Cơ chế hoạt động của @Retryable:
 *    1. Spring tạo AOP Proxy quanh bean này (nhờ @EnableRetry)
 *    2. Khi deleteCache() được gọi, proxy intercept lời gọi
 *    3. Nếu method ném exception thuộc loại được chỉ định (retryFor)
 *       → Proxy tự động gọi lại method sau khoảng delay (backoff)
 *    4. Sau maxAttempts lần thất bại → proxy gọi @Recover method
 *
 *  =============================================================================
 *  SOLUTION 2: CDC - Change Data Capture Pattern
 *  =============================================================================
 *
 *  Ý tưởng: Thay vì xóa cache trực tiếp bên trong @Transactional (nguy hiểm),
 *  ta publish 1 event và chỉ xóa cache SAU KHI transaction DB đã commit xong.
 *
 *  @TransactionalEventListener(phase = AFTER_COMMIT):
 *    - Listener này KHÔNG chạy trong transaction hiện tại
 *    - Nó chờ cho đến khi transaction COMMIT thành công 100% rồi mới kích hoạt
 *    - Nếu transaction ROLLBACK → listener KHÔNG chạy (tránh xóa cache oan)
 *
 *  Timeline so sánh:
 *
 *  ❌ Cách cũ (synchronous, trong @Transactional):
 *    [BEGIN TX] → save() → redis.delete() → [COMMIT]
 *                            ↑
 *                       Có thể fail trước khi commit!
 *                       Và nếu DB rollback sau đó, cache đã bị xóa rồi.
 *
 *  ✅ Cách CDC (event-driven, AFTER_COMMIT):
 *    [BEGIN TX] → save() → publish(event) → [COMMIT] → listener: redis.delete()
 *                                                           ↑
 *                                          Chỉ chạy sau khi DB đã commit chắc chắn!
 *
 *  Trong thực tế production:
 *    Cách này được implement qua Debezium đọc MySQL/PostgreSQL binlog và
 *    publish event lên Kafka. Service cache lắng nghe Kafka topic và invalidate.
 *    Đây là CDC (Change Data Capture) thực sự — hoàn toàn tách biệt với
 *    application code.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheInvalidationService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_PREFIX = "user:";

    // =========================================================================
    // SOLUTION 1: Retry với @Retryable
    // =========================================================================

    /**
     * Xóa cache với cơ chế Retry tự động.
     *
     * @Retryable parameters:
     *
     * - retryFor: Exception nào sẽ trigger retry?
     *   → RedisConnectionFailureException: Redis bị ngắt kết nối
     *   → RuntimeException: Các lỗi runtime khác từ Redis
     *
     * - maxAttempts = 3: Thử tối đa 3 lần (1 lần gốc + 2 lần retry)
     *
     * - backoff: Khoảng chờ giữa các lần retry
     *   → delay = 500: Chờ 500ms trước khi retry lần 1
     *   → multiplier = 2.0: Lần retry tiếp theo chờ gấp đôi (Exponential Backoff)
     *     Lần 1: 500ms → Lần 2: 1000ms → Lần 3: 2000ms
     *
     *  TẠI SAO DÙNG EXPONENTIAL BACKOFF?
     *  Nếu Redis đang quá tải, retry ngay lập tức có thể làm tình hình tệ hơn.
     *  Exponential Backoff cho Redis có thêm thời gian hồi phục.
     *
     * QUAN TRỌNG: @Retryable chỉ hoạt động khi method được gọi từ BÊN NGOÀI bean
     * (thông qua Spring proxy). Gọi self.deleteCache() trong cùng class sẽ không retry!
     */
    @Retryable(
            retryFor = {RedisConnectionFailureException.class, RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2.0)
    )
    public void deleteCache(Long userId) {
        String cacheKey = CACHE_KEY_PREFIX + userId;

        log.debug("[CACHE INVALIDATE] Attempting to delete key={}", cacheKey);

        // Đây là thao tác có thể THẤT BẠI:
        // - Redis Server đang down
        // - Network timeout
        // - Connection pool exhausted
        Boolean deleted = redisTemplate.delete(cacheKey);

        log.debug("[CACHE INVALIDATE] key={} deleted={}", cacheKey, deleted);
    }

    /**
     * @Recover: Method dự phòng khi TẤT CẢ các lần retry đều thất bại.
     *
     * Khi đã thử đủ maxAttempts (3) lần mà vẫn fail, Spring Retry
     * tự động gọi method này thay vì ném exception ra caller.
     *
     * Signature bắt buộc của @Recover:
     *  - Return type: PHẢI khớp với method @Retryable (void)
     *  - Tham số đầu: Exception type khớp với retryFor
     *  - Tham số sau: CÓ THỂ giữ nguyên tham số của method gốc (Long userId)
     *
     * Trong production, tại đây bạn nên:
     *  1. Ghi vào Dead Letter Queue (DLQ) để retry sau
     *  2. Gửi alert tới team (PagerDuty, Slack...)
     *  3. Ghi vào outbox table để retry theo schedule
     */
    @Recover
    public void recoverDeleteCache(RuntimeException exception, Long userId) {
        // Sau 3 lần retry đều thất bại:
        log.error("[CACHE INVALIDATE FAILED] All {} retries exhausted for userId={}. " +
                  "Cache may be stale until TTL expires. Error: {}",
                  3, userId, exception.getMessage());

        // TODO (Production): Ghi vào outbox table hoặc gửi vào message queue
        // để có một worker khác retry sau một khoảng thời gian dài hơn.
        //
        // Ví dụ pseudo-code:
        //   outboxRepository.save(new CacheInvalidationTask(userId, Instant.now()));
    }

    // =========================================================================
    // SOLUTION 2: CDC Pattern — @TransactionalEventListener(AFTER_COMMIT)
    // =========================================================================

    /**
     * CDC Listener: Lắng nghe sự kiện UserProfileUpdatedEvent và xóa cache
     * CHỈ SAU KHI transaction DB đã commit thành công.
     *
     * @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT):
     *
     *   AFTER_COMMIT  → Chạy sau khi transaction commit ✅ (dùng cho invalidate cache)
     *   AFTER_ROLLBACK → Chạy khi transaction rollback (dùng để rollback side effects)
     *   AFTER_COMPLETION → Chạy dù commit hay rollback (dùng để cleanup)
     *   BEFORE_COMMIT   → Chạy ngay trước khi commit (dùng để validate)
     *
     * TẠI SAO AFTER_COMMIT AN TOÀN HƠN?
     *
     * Kịch bản nguy hiểm với xóa cache TRONG @Transactional:
     *   [BEGIN TX]
     *   → db.save(user, age=34)     ← DB chưa commit!
     *   → redis.delete("user:1")    ← Cache bị xóa sớm
     *   → [EXCEPTION XẢY RA]
     *   → [ROLLBACK] DB về age=33
     *
     *   Kết quả: DB=33, Cache=rỗng
     *   → Lần đọc tiếp: Cache Miss → DB → age=33 ghi ngược lên cache
     *   → Client thấy age=33 (cũ) dù tưởng đã update
     *
     * Với AFTER_COMMIT:
     *   [BEGIN TX]
     *   → db.save(user, age=34)
     *   → publish(UserProfileUpdatedEvent(1))  ← chỉ publish event, chưa xóa cache
     *   → [EXCEPTION XẢY RA]
     *   → [ROLLBACK] DB về age=33
     *   → Listener KHÔNG CHẠY (vì không có COMMIT)
     *
     *   Kết quả: DB=33, Cache=vẫn còn dữ liệu cũ ✅ (nhất quán với DB)
     *
     * Kịch bản thành công:
     *   [BEGIN TX]
     *   → db.save(user, age=34)
     *   → publish(UserProfileUpdatedEvent(1))
     *   → [COMMIT] ← DB đã ghi age=34 vĩnh viễn
     *   → Listener chạy: redis.delete("user:1") ← xóa cache an toàn
     *
     *   Kết quả: DB=34, Cache=rỗng ✅
     *   → Lần đọc tiếp: Cache Miss → DB → age=34 ✅
     *
     * @param event Event được publish từ UserProfileService
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserProfileUpdated(UserProfileUpdatedEvent event) {
        Long userId = event.userId();
        log.debug("[CDC LISTENER] Transaction committed. Invalidating cache for userId={}", userId);

        // Gọi deleteCache() với retry (Solution 1 + Solution 2 kết hợp!)
        // → CDC đảm bảo chỉ chạy sau commit
        // → Retry đảm bảo tự gọi lại nếu Redis tạm thời không phản hồi
        deleteCache(userId);
    }
}
