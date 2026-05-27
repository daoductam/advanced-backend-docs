package com.tamdao.security.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String displayName;

    // Personal Data Encryption (Encrypted email stored as binary)
    @Lob
    @Column(name = "email_encrypted")
    private byte[] emailEncrypted;

    // Blind Index for fast exact match searches (indexed)
    @Column(name = "email_blind_index", length = 64)
    private String emailBlindIndex;

    // Encrypted phone stored as binary
    @Lob
    @Column(name = "phone_encrypted")
    private byte[] phoneEncrypted;

    @Column(name = "phone_blind_index", length = 64)
    private String phoneBlindIndex;

    @Column(name = "balance")
    private Long balance = 10000L; // Start with 10,000 for CSRF transfer simulation

    // Link User to Role
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    public User() {}

    public User(String username, String passwordHash, String displayName) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public byte[] getEmailEncrypted() {
        return emailEncrypted;
    }

    public void setEmailEncrypted(byte[] emailEncrypted) {
        this.emailEncrypted = emailEncrypted;
    }

    public String getEmailBlindIndex() {
        return emailBlindIndex;
    }

    public void setEmailBlindIndex(String emailBlindIndex) {
        this.emailBlindIndex = emailBlindIndex;
    }

    public byte[] getPhoneEncrypted() {
        return phoneEncrypted;
    }

    public void setPhoneEncrypted(byte[] phoneEncrypted) {
        this.phoneEncrypted = phoneEncrypted;
    }

    public String getPhoneBlindIndex() {
        return phoneBlindIndex;
    }

    public void setPhoneBlindIndex(String phoneBlindIndex) {
        this.phoneBlindIndex = phoneBlindIndex;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
