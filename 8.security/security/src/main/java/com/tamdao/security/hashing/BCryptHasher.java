package com.tamdao.security.hashing;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptHasher implements PasswordHasher {

    private final BCryptPasswordEncoder encoder;
    private final int strength;

    public BCryptHasher(int strength) {
        this.strength = strength;
        // Strength is the log rounds (4 to 31). Default is 10.
        this.encoder = new BCryptPasswordEncoder(strength);
    }

    @Override
    public String hash(String plaintext) {
        return encoder.encode(plaintext);
    }

    @Override
    public boolean verify(String plaintext, String hashed) {
        return encoder.matches(plaintext, hashed);
    }

    @Override
    public String getAlgorithmName() {
        return "BCrypt (Strength: " + strength + ")";
    }
}
