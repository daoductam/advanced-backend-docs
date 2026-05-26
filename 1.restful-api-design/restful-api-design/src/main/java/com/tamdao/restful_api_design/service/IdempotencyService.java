package com.tamdao.restful_api_design.service;

import com.tamdao.restful_api_design.model.IdempotencyRecord;
import com.tamdao.restful_api_design.repository.IdempotencyRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.redis.core.StringRedisTemplate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class IdempotencyService {

    @Autowired
    private IdempotencyRecordRepository repository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String REDIS_LOCK_PREFIX = "lock:idempotency:";

    /**
     * Bước 1: Tra cứu thông tin từ DB H2 (Storage chứa kết quả chính thức)
     */
    @Transactional(readOnly = true)
    public Optional<IdempotencyRecord> findRecord(String key) {
        return repository.findById(key);
    }

    /**
     * MÔ HÌNH LAI - BƯỚC 2: Dùng Redis làm Distributed Lock trong 10 giây để chặn ngay lập tức
     * các request bấm đúp song song cực nhanh gửi đồng thời (Double Submit).
     * Trả về true nếu giành khóa lock thành công, false nếu trùng lặp.
     */
    public boolean lockKey(String key) {
        // Kiểm tra xem giao dịch đã hoàn thành và lưu trong DB chưa trước khi tạo lock mới
        if (repository.existsById(key)) {
            return false;
        }

        String redisKey = REDIS_LOCK_PREFIX + key;
        
        // Sử dụng lệnh SETNX (Set if Not Exists) với thời gian hết hạn TTL 10 giây để làm Distributed Lock
        Boolean success = redisTemplate.opsForValue().setIfAbsent(redisKey, "PROCESSING", Duration.ofSeconds(10));
        
        return success != null && success;
    }

    /**
     * MÔ HÌNH LAI - BƯỚC 3: Lưu kết quả phản hồi cuối cùng vào DB H2 (Persistent Storage)
     * đồng thời xóa Lock trên Redis để giải phóng tài nguyên.
     */
    @Transactional
    public void saveResult(String key, int status, String responseBody) {
        IdempotencyRecord record = new IdempotencyRecord();
        record.setIdempotencyKey(key);
        record.setResponseStatus(status);
        record.setResponseBody(responseBody);
        record.setCreatedAt(LocalDateTime.now());
        
        // Lưu trữ lâu dài trong RDBMS
        repository.save(record);

        // Giải phóng lock trên Redis ngay lập tức
        redisTemplate.delete(REDIS_LOCK_PREFIX + key);
    }

    /**
     * Giải phóng Lock trên Redis nếu xử lý nghiệp vụ chính gặp lỗi runtime hệ thống
     * để Client có thể thử gửi lại lần sau.
     */
    public void unlockKey(String key) {
        redisTemplate.delete(REDIS_LOCK_PREFIX + key);
    }
}
