package com.tamdao.security.service;

import com.tamdao.security.dto.UserLoginDto;
import com.tamdao.security.dto.UserRegisterDto;
import com.tamdao.security.dto.UserResponseDto;
import com.tamdao.security.entity.Role;
import com.tamdao.security.entity.User;
import com.tamdao.security.hashing.PasswordHasher;
import com.tamdao.security.repository.RoleRepository;
import com.tamdao.security.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.Collections;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordHasher passwordHasher;
    private final EncryptionService encryptionService;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, 
                           PasswordHasher passwordHasher, EncryptionService encryptionService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordHasher = passwordHasher;
        this.encryptionService = encryptionService;
    }

    @Override
    @Transactional
    public UserResponseDto register(UserRegisterDto registerDto) {
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            throw new IllegalArgumentException("Username already exists!");
        }

        // 1. Hash the password (Argon2id)
        String hashedPassword = passwordHasher.hash(registerDto.getPassword());

        User user = new User(registerDto.getUsername(), hashedPassword, registerDto.getDisplayName());

        // 2. Encrypt Email and Phone
        if (registerDto.getEmail() != null) {
            user.setEmailEncrypted(encryptionService.encrypt(registerDto.getEmail()));
            user.setEmailBlindIndex(encryptionService.generateBlindIndex(registerDto.getEmail()));
        }
        if (registerDto.getPhone() != null) {
            user.setPhoneEncrypted(encryptionService.encrypt(registerDto.getPhone()));
            user.setPhoneBlindIndex(encryptionService.generateBlindIndex(registerDto.getPhone()));
        }

        // 3. Assign Default Role (ROLE_USER)
        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Default role ROLE_USER not initialized!"));
        user.setRole(defaultRole);

        User savedUser = userRepository.save(user);

        String emailEncryptedBase64 = savedUser.getEmailEncrypted() != null 
            ? Base64.getEncoder().encodeToString(savedUser.getEmailEncrypted()) : null;

        return new UserResponseDto(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getDisplayName(),
                savedUser.getPasswordHash(),
                registerDto.getEmail(),
                registerDto.getPhone(),
                emailEncryptedBase64,
                savedUser.getEmailBlindIndex(),
                savedUser.getRole().getName(),
                savedUser.getRole().getPermissions(),
                "User registered successfully with default ROLE_USER."
        );
    }

    @Override
    public UserResponseDto login(UserLoginDto loginDto) {
        User user = userRepository.findByUsername(loginDto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password!"));

        boolean matches = passwordHasher.verify(loginDto.getPassword(), user.getPasswordHash());
        if (!matches) {
            throw new IllegalArgumentException("Invalid username or password!");
        }

        String decryptedEmail = encryptionService.decrypt(user.getEmailEncrypted());
        String decryptedPhone = encryptionService.decrypt(user.getPhoneEncrypted());
        String emailEncryptedBase64 = user.getEmailEncrypted() != null 
            ? Base64.getEncoder().encodeToString(user.getEmailEncrypted()) : null;

        String roleName = user.getRole() != null ? user.getRole().getName() : null;
        Set<String> permissions = user.getRole() != null ? user.getRole().getPermissions() : Collections.emptySet();

        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getPasswordHash(),
                decryptedEmail,
                decryptedPhone,
                emailEncryptedBase64,
                user.getEmailBlindIndex(),
                roleName,
                permissions,
                "Login successful!"
        );
    }

    @Override
    public UserResponseDto searchByEmail(String email) {
        String searchBlindIndex = encryptionService.generateBlindIndex(email);

        User user = userRepository.findByEmailBlindIndex(searchBlindIndex)
                .orElseThrow(() -> new IllegalArgumentException("No user found with the provided email!"));

        String decryptedEmail = encryptionService.decrypt(user.getEmailEncrypted());
        String decryptedPhone = encryptionService.decrypt(user.getPhoneEncrypted());
        String emailEncryptedBase64 = user.getEmailEncrypted() != null 
            ? Base64.getEncoder().encodeToString(user.getEmailEncrypted()) : null;

        String roleName = user.getRole() != null ? user.getRole().getName() : null;
        Set<String> permissions = user.getRole() != null ? user.getRole().getPermissions() : Collections.emptySet();

        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getPasswordHash(),
                decryptedEmail,
                decryptedPhone,
                emailEncryptedBase64,
                user.getEmailBlindIndex(),
                roleName,
                permissions,
                "User found!"
        );
    }

    @Override
    @Transactional
    public void updateRolePermissions(String roleName, Set<String> newPermissions) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
        role.getPermissions().clear();
        role.getPermissions().addAll(newPermissions);
        roleRepository.save(role);
    }
}
