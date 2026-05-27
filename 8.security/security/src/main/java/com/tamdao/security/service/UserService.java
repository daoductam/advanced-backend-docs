package com.tamdao.security.service;

import com.tamdao.security.dto.UserLoginDto;
import com.tamdao.security.dto.UserRegisterDto;
import com.tamdao.security.dto.UserResponseDto;

import java.util.Set;

public interface UserService {
    UserResponseDto register(UserRegisterDto registerDto);
    UserResponseDto login(UserLoginDto loginDto);
    UserResponseDto searchByEmail(String email);
    
    // Dynamically update permissions mapped to a Role
    void updateRolePermissions(String roleName, Set<String> newPermissions);
}
