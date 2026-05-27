package com.tamdao.security.hashing;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

public class Argon2Hasher implements PasswordHasher {

    private final Argon2PasswordEncoder encoder;
    private final String configDesc;

    public Argon2Hasher() {
        // Default settings for Spring Security: saltLength=16, hashLength=32, parallelism=1, memory=16384 KB (16 MB), iterations=2
        this.encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        this.configDesc = "Argon2id (Defaults: Memory=16MB, Iterations=2, Parallelism=1)";
    }

    public Argon2Hasher(int saltLength, int hashLength, int parallelism, int memory, int iterations) {
        this.encoder = new Argon2PasswordEncoder(saltLength, hashLength, parallelism, memory, iterations);
        this.configDesc = String.format("Argon2id (Custom: Memory=%dKB, Iterations=%d, Parallelism=%d)", memory, iterations, parallelism);
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
        return configDesc;
    }
}
