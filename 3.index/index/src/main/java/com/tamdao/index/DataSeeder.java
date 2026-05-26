package com.tamdao.index;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Component hỗ trợ tự động khởi tạo dữ liệu ảo (1.000.000 dòng) trên MySQL
 * để phục vụ đo lường hiệu năng Database Index.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("[SEEDER] Đang kiểm tra bảng orders...");
        
        // 1. Tạo bảng nếu chưa tồn tại
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS orders (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                order_code VARCHAR(50) NOT NULL,
                customer_id BIGINT NOT NULL,
                status VARCHAR(20) NOT NULL,
                amount DECIMAL(15,2) NOT NULL,
                created_at TIMESTAMP NOT NULL,
                shipping_address VARCHAR(255) NOT NULL
            )
        """);

        // 2. Kiểm tra xem đã có dữ liệu chưa
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);
        if (count != null && count > 0) {
            log.info("[SEEDER] Bảng orders đã có sẵn {} dòng dữ liệu. Bỏ qua bước seed.", count);
            return;
        }

        // 3. Tiến hành insert dữ liệu lớn theo Batch
        log.info("[SEEDER] Bắt đầu sinh 1,000,000 dòng dữ liệu ảo (Vui lòng đợi khoảng 1-2 phút)...");
        
        String sql = "INSERT INTO orders (order_code, customer_id, status, amount, created_at, shipping_address) VALUES (?, ?, ?, ?, ?, ?)";
        List<Object[]> batchArgs = new ArrayList<>();
        Random random = new Random();
        String[] statuses = {"PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"};
        
        long start = System.currentTimeMillis();
        
        for (int i = 1; i <= 1000000; i++) {
            batchArgs.add(new Object[]{
                "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-" + i,
                (long) (random.nextInt(10000) + 1), // 10,000 khách hàng ngẫu nhiên
                statuses[random.nextInt(statuses.length)],
                BigDecimal.valueOf(10 + (random.nextDouble() * 990)), // Đơn hàng 10$ -> 1000$
                new Timestamp(System.currentTimeMillis() - (random.nextInt(365) * 24L * 3600 * 1000)), // Tạo trong vòng 1 năm qua
                "Address number " + i + ", Street XYZ, District ABC"
            });

            if (i % 10000 == 0) {
                jdbcTemplate.batchUpdate(sql, batchArgs);
                batchArgs.clear();
                log.info("[SEEDER] Đã sinh thành công: {} / 1,000,000 dòng...", i);
            }
        }
        
        long end = System.currentTimeMillis();
        log.info("[SEEDER] Hoàn thành sinh dữ liệu! Tổng thời gian thực thi: {} giây.", (end - start) / 1000);
    }
}
