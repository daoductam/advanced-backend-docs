package com.tamdao.security.dto;

import java.util.Set;

public class UserResponseDto {
    private Long id;
    private String username;
    private String displayName;
    private String passwordHash;
    
    // Decrypted (original) values returned for confirmation
    private String email;
    private String phone;
    
    // Encrypted representations stored in DB (returned for demonstration)
    private String emailEncryptedBase64;
    private String emailBlindIndex;
    
    // Authorization info
    private String role;
    private Set<String> permissions;

    private String statusMessage;

    public UserResponseDto() {}

    public UserResponseDto(Long id, String username, String displayName, String passwordHash, 
                           String email, String phone, String emailEncryptedBase64, String emailBlindIndex, 
                           String role, Set<String> permissions, String statusMessage) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
        this.email = email;
        this.phone = phone;
        this.emailEncryptedBase64 = emailEncryptedBase64;
        this.emailBlindIndex = emailBlindIndex;
        this.role = role;
        this.permissions = permissions;
        this.statusMessage = statusMessage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmailEncryptedBase64() {
        return emailEncryptedBase64;
    }

    public void setEmailEncryptedBase64(String emailEncryptedBase64) {
        this.emailEncryptedBase64 = emailEncryptedBase64;
    }

    public String getEmailBlindIndex() {
        return emailBlindIndex;
    }

    public void setEmailBlindIndex(String emailBlindIndex) {
        this.emailBlindIndex = emailBlindIndex;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
