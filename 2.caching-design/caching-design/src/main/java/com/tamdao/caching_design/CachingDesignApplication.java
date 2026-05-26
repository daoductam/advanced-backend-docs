package com.tamdao.caching_design;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @EnableRetry: Kích hoạt AOP proxy cho @Retryable (Problem 01 - No Atomicity)
 *
 * @EnableScheduling: Kích hoạt @Scheduled annotation (Problem 03 - Cache Breakdown)
 *   → Cho phép Spring Boot chạy các method được đánh dấu @Scheduled định kỳ
 *   → Background Job sẽ tự động refresh hotspot cache trước khi TTL hết hạn
 *   → Nếu thiếu @EnableScheduling → @Scheduled bị bỏ qua hoàn toàn!
 */
@SpringBootApplication
@EnableRetry
@EnableScheduling
public class CachingDesignApplication {

	public static void main(String[] args) {
		SpringApplication.run(CachingDesignApplication.class, args);
	}

}
