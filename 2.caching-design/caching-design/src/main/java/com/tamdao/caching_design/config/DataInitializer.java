package com.tamdao.caching_design.config;

import com.tamdao.caching_design.model.UserProfile;
import com.tamdao.caching_design.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * DataInitializer chạy ngay sau khi Spring Boot khởi động thành công.
 * Nạp dữ liệu mẫu vào H2 Database để có thể test ngay.
 *
 * Implements CommandLineRunner: Spring Boot tự động gọi run() sau khi context ready.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserProfileRepository userProfileRepository;

    @Override
    public void run(String... args) {
        log.info("=== Seeding initial data into H2 Database ===");

        // Tạo 3 user mẫu để test
        userProfileRepository.save(UserProfile.builder()
                .name("Tam Dao")
                .age(25)
                .email("tam@mail.com")
                .build());

        userProfileRepository.save(UserProfile.builder()
                .name("Nguyen Van A")
                .age(30)
                .email("nguyenvana@mail.com")
                .build());

        userProfileRepository.save(UserProfile.builder()
                .name("Tran Thi B")
                .age(22)
                .email("tranthib@mail.com")
                .build());

        log.info("=== Seeded {} users. Test the cache at: GET http://localhost:8080/api/users/1 ===",
                userProfileRepository.count());
    }
}
