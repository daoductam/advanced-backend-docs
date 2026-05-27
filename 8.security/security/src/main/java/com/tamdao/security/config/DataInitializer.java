package com.tamdao.security.config;

import com.tamdao.security.entity.Role;
import com.tamdao.security.entity.User;
import com.tamdao.security.hashing.PasswordHasher;
import com.tamdao.security.repository.RoleRepository;
import com.tamdao.security.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository, PasswordHasher passwordHasher) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Initialize Roles & Permissions
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
            Role role = new Role("ROLE_ADMIN");
            role.setPermissions(Set.of("DOCUMENT_READ", "DOCUMENT_WRITE", "DOCUMENT_DELETE"));
            return roleRepository.save(role);
        });

        Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
            Role role = new Role("ROLE_USER");
            role.setPermissions(Set.of("DOCUMENT_READ"));
            return roleRepository.save(role);
        });

        // 2. Initialize Users
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User("admin", passwordHasher.hash("adminPass123"), "System Administrator");
            admin.setRole(adminRole);
            userRepository.save(admin);
        }

        if (!userRepository.existsByUsername("user1")) {
            User user = new User("user1", passwordHasher.hash("userPass123"), "Normal User");
            user.setRole(userRole);
            userRepository.save(user);
        }

        System.out.println(">>> ĐÃ KHỞI TẠO CSDL THỰC HÀNH: ");
        System.out.println("  * Tài khoản ADMIN: admin / adminPass123 (Quyền: DOCUMENT_READ, DOCUMENT_WRITE, DOCUMENT_DELETE)");
        System.out.println("  * Tài khoản USER:  user1 / userPass123 (Quyền: DOCUMENT_READ)");
    }
}
