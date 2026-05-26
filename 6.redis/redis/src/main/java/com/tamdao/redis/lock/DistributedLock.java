package com.tamdao.redis.lock;

public interface DistributedLock {
    /**
     * Thu thập khóa phân tán.
     * 
     * @param lockKey Key khóa phân tán trên Redis.
     * @param lockValue Giá trị định danh duy nhất cho luồng đang giữ khóa (ví dụ: UUID).
     * @param expireTimeInMs Thời gian hết hạn của khóa (TTL) tính bằng mili giây.
     * @return true nếu lấy được khóa thành công, ngược lại trả về false.
     */
    boolean acquire(String lockKey, String lockValue, long expireTimeInMs);

    /**
     * Giải phóng khóa phân tán một cách an toàn.
     * 
     * @param lockKey Key khóa phân tán trên Redis.
     * @param lockValue Giá trị định danh duy nhất của luồng đang giữ khóa (phải khớp với giá trị lúc acquire).
     * @return true nếu giải phóng thành công, ngược lại trả về false.
     */
    boolean release(String lockKey, String lockValue);
}
