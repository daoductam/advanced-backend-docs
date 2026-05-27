package com.tamdao.security.hashing;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Hasher implements PasswordHasher {

    @Override
    public String hash(String plaintext) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashInBytes = md.digest(plaintext.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashInBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    @Override
    public boolean verify(String plaintext, String hashed) {
        // MD5 is deterministic and unsalted here, so we just hash the input and compare
        return hash(plaintext).equals(hashed);
    }

    @Override
    public String getAlgorithmName() {
        return "MD5 (Insecure, No Salt)";
    }
}
