package com.tamdao.caching_design.event;

/**
 * =============================================================================
 *  DOMAIN EVENT: UserProfileUpdatedEvent
 * =============================================================================
 *
 *  Đây là "tín hiệu" (event) được phát ra khi một UserProfile thay đổi trong DB.
 *
 *  TẠI SAO DÙNG EVENT?
 *
 *  Trong giải pháp CDC (Change Data Capture - README 3.1.1 Solution 2),
 *  chúng ta muốn invalidate cache CHỈ SAU KHI DB đã commit xong.
 *
 *  Vấn đề với cách gọi thẳng (synchronous delete cache trong @Transactional):
 *
 *    @Transactional
 *    void update() {
 *        db.save(user);          // ← DB transaction chưa commit!
 *        redis.delete(cacheKey); // ← Xóa cache ngay đây, nhưng DB chưa commit xong
 *        // Nếu sau đó DB rollback → Cache đã bị xóa rồi!
 *        // Client lần đọc tiếp: Cache Miss → DB (data cũ) → ghi lại cache cũ
 *    }
 *
 *  Với Event + @TransactionalEventListener(AFTER_COMMIT):
 *
 *    @Transactional
 *    void update() {
 *        db.save(user);
 *        publish(new UserProfileUpdatedEvent(userId)); // ← chỉ publish event
 *        // Transaction commit → AFTER_COMMIT listener được kích hoạt
 *    }
 *    // → Cache chỉ bị xóa SAU KHI DB đã commit chắc chắn 100%
 *    // → Nếu DB rollback, listener không chạy → cache không bị xóa oan
 *
 *  Trong thực tế production:
 *  Thay vì ApplicationEventPublisher (in-process), người ta dùng
 *  Debezium (đọc MySQL/Postgres binlog) + Kafka để CDC ra bên ngoài,
 *  cho phép nhiều service khác nhau lắng nghe sự kiện DB thay đổi.
 *
 * @param userId ID của user vừa được thay đổi trong DB
 */
public record UserProfileUpdatedEvent(Long userId) {
}
