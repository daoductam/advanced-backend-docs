package com.tamdao.redis.service;

import com.tamdao.redis.lock.DistributedLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    /**
     * Thực hiện đặt chỗ sử dụng khóa phân tán truyền vào.
     * 
     * @param customerName Tên khách hàng.
     * @param flightCode Mã chuyến bay.
     * @param seatNumber Số ghế.
     * @param lock Lock implementation (Custom Lock hoặc Redisson Lock).
     * @return true nếu đặt chỗ thành công, false nếu thất bại do tranh chấp khóa.
     */
    public boolean bookSeat(String customerName, String flightCode, String seatNumber, DistributedLock lock) {
        String lockKey = String.format("lock:flight:%s:seat:%s", flightCode, seatNumber);
        String lockValue = UUID.randomUUID().toString(); // Token định danh duy nhất cho yêu cầu này
        long expireTimeInMs = 5000; // Khóa tự động hết hạn sau 5 giây để tránh deadlock

        log.info("[Booking Action] {} is trying to book seat {} on flight {}...", customerName, seatNumber, flightCode);

        // Thu thập khóa
        boolean acquired = lock.acquire(lockKey, lockValue, expireTimeInMs);

        if (!acquired) {
            log.error("[Booking Conflict] {} FAILED to book seat {}. Seat is currently locked by another transaction.", customerName, seatNumber);
            return false;
        }

        try {
            log.info("[Booking Processing] {} acquired lock. Processing database save and payment...", customerName);
            // Giả lập thời gian ghi DB & thanh toán (2.5 giây)
            Thread.sleep(2500);
            log.info("[Booking Success] {} successfully booked seat {} on flight {}!", customerName, seatNumber, flightCode);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[Booking Error] Process interrupted for " + customerName, e);
            return false;
        } finally {
            // Giải phóng khóa an toàn
            lock.release(lockKey, lockValue);
        }
    }
}
