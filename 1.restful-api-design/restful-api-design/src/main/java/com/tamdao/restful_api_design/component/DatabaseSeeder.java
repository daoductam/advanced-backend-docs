package com.tamdao.restful_api_design.component;

import com.tamdao.restful_api_design.model.User;
import com.tamdao.restful_api_design.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            List<User> users = new ArrayList<>();
            for (int i = 1; i <= 50; i++) {
                users.add(User.builder()
                        .username("user_" + i)
                        .email("user" + i + "@gmail.com")
                        .createdAt(LocalDateTime.now().minusMinutes(50 - i))
                        .build());
            }
            userRepository.saveAll(users);
            System.out.println(">>> Đã seeding 50 users giả lập thành công vào H2 Database.");
        }
    }
}
