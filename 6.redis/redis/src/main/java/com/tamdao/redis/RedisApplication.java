package com.tamdao.redis;

import com.tamdao.redis.lock.impl.CustomRedisLock;
import com.tamdao.redis.lock.impl.RedissonLockImpl;
import com.tamdao.redis.service.BookingService;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class RedisApplication {

    private static final Logger log = LoggerFactory.getLogger(RedisApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(RedisApplication.class, args);
    }

    // Định nghĩa Bean cho RedissonClient
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
              .setAddress("redis://127.0.0.1:6379")
              .setConnectionMinimumIdleSize(2)
              .setConnectionPoolSize(8);
        return Redisson.create(config);
    }

    // Khởi chạy trình giả lập đặt vé song song để kiểm chứng khóa
    @Bean
    public CommandLineRunner runSimulation(
            BookingService bookingService,
            CustomRedisLock customRedisLock,
            RedissonLockImpl redissonLock) {
        return args -> {
            log.info("====================================================================");
            log.info("🏁 KHỞI CHẠY MÔ PHỎNG ĐẶT VÉ ĐỒNG THỜI (CONCURRENT FLIGHT BOOKING)");
            log.info("====================================================================");

            String flightCode = "FA634";
            String seatNumber = "A34_S012C";

            // 1. Chạy thử nghiệm với Custom Lock (RedisTemplate + Lua Script)
            log.info("\n>>> [TEST CASE 1] Sử dụng Custom Redis Lock (RedisTemplate + Lua Script):");
            runConcurrentBooking(bookingService, customRedisLock, flightCode, seatNumber);

            // Chờ một chút trước khi sang Test Case 2
            Thread.sleep(4000);

            // 2. Chạy thử nghiệm với Redisson Lock
            log.info("\n>>> [TEST CASE 2] Sử dụng Redisson Lock:");
            runConcurrentBooking(bookingService, redissonLock, flightCode, seatNumber);

            log.info("====================================================================");
            log.info("🏁 MÔ PHỎNG KẾT THÚC");
            log.info("====================================================================");
        };
    }

    private void runConcurrentBooking(
            BookingService bookingService,
            com.tamdao.redis.lock.DistributedLock lockImpl,
            String flightCode,
            String seatNumber) {

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Khách hàng 1 gửi yêu cầu
        executor.submit(() -> {
            Thread.currentThread().setName("Customer-1-Thread");
            bookingService.bookSeat("Customer-1 (Nguyễn Văn A)", flightCode, seatNumber, lockImpl);
        });

        // Khách hàng 2 gửi yêu cầu đồng thời (ngay sau đó vài mili giây)
        executor.submit(() -> {
            Thread.currentThread().setName("Customer-2-Thread");
            try {
                Thread.sleep(50); // Delay cực nhỏ để mô phỏng sự bất đồng bộ thực tế
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            bookingService.bookSeat("Customer-2 (Trần Thị B)", flightCode, seatNumber, lockImpl);
        });

        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
