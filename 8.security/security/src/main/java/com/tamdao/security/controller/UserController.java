package com.tamdao.security.controller;

import com.tamdao.security.dto.UserLoginDto;
import com.tamdao.security.dto.UserRegisterDto;
import com.tamdao.security.dto.UserResponseDto;
import com.tamdao.security.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody UserRegisterDto registerDto) {
        UserResponseDto response = userService.register(registerDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> login(@RequestBody UserLoginDto loginDto) {
        UserResponseDto response = userService.login(loginDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<UserResponseDto> searchByEmail(@RequestParam String email) {
        UserResponseDto response = userService.searchByEmail(email);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
