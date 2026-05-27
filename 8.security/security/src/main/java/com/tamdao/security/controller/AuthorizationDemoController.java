package com.tamdao.security.controller;

import com.tamdao.security.entity.User;
import com.tamdao.security.repository.UserRepository;
import com.tamdao.security.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/authz")
public class AuthorizationDemoController {

    private final UserRepository userRepository;
    private final UserService userService;

    public AuthorizationDemoController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    // Helper method to simulate security context from header X-Auth-User
    private User getAuthenticatedUser(String usernameHeader) {
        if (usernameHeader == null || usernameHeader.isBlank()) {
            throw new IllegalArgumentException("Missing X-Auth-User header! (Nhập 'admin' hoặc 'user1')");
        }
        return userRepository.findByUsername(usernameHeader)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + usernameHeader));
    }

    // ======================================================
    // 1. ROLE-BASED ACCESS CONTROL (RBAC) DEMO
    // ======================================================
    
    @GetMapping("/rbac/admin-only")
    public ResponseEntity<String> rbacAdminOnly(@RequestHeader("X-Auth-User") String usernameHeader) {
        try {
            User user = getAuthenticatedUser(usernameHeader);
            
            // RBAC Check: Hardcoded check on Role Name
            if (!"ROLE_ADMIN".equals(user.getRole().getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Từ chối truy cập (RBAC)! API này yêu cầu vai trò ROLE_ADMIN. Vai trò hiện tại của bạn: " 
                                + user.getRole().getName());
            }
            
            return ResponseEntity.ok("Chào Admin (" + user.getDisplayName() 
                    + ")! Bạn đã truy cập thành công vào khu vực bảo mật vai trò ADMIN.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ======================================================
    // 2. PERMISSION-BASED ACCESS CONTROL DEMO
    // ======================================================

    @GetMapping("/permissions/document")
    public ResponseEntity<String> readDocument(@RequestHeader("X-Auth-User") String usernameHeader) {
        try {
            User user = getAuthenticatedUser(usernameHeader);
            
            // Permission-based Check: Check if user's role contains specific permission
            if (!user.getRole().getPermissions().contains("DOCUMENT_READ")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Từ chối truy cập (Permission-based)! Bạn thiếu quyền 'DOCUMENT_READ'.");
            }
            
            return ResponseEntity.ok("Đọc tài liệu thành công! (" + user.getDisplayName() 
                    + " có quyền DOCUMENT_READ)");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/permissions/document")
    public ResponseEntity<String> writeDocument(@RequestHeader("X-Auth-User") String usernameHeader) {
        try {
            User user = getAuthenticatedUser(usernameHeader);
            
            // Permission-based Check: Check for write access
            if (!user.getRole().getPermissions().contains("DOCUMENT_WRITE")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Từ chối truy cập (Permission-based)! Bạn thiếu quyền 'DOCUMENT_WRITE'.");
            }
            
            return ResponseEntity.ok("Tạo tài liệu mới thành công! (" + user.getDisplayName() 
                    + " có quyền DOCUMENT_WRITE)");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ======================================================
    // 3. ADMIN PORTAL SIMULATION (DYNAMICAL PERMISSION MAP)
    // ======================================================

    @PutMapping("/admin/roles/{roleName}/permissions")
    public ResponseEntity<String> updateRolePermissions(@PathVariable String roleName, 
                                                        @RequestBody Set<String> permissions) {
        try {
            userService.updateRolePermissions(roleName, permissions);
            return ResponseEntity.ok("CẬP NHẬT THÀNH CÔNG! Đã cập nhật danh sách quyền cho vai trò " 
                    + roleName + " thành: " + permissions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}
