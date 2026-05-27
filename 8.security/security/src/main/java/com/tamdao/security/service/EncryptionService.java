package com.tamdao.security.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {

    // AES-GCM parameters
    private static final String AES_ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 12 bytes IV is standard for GCM
    private static final int GCM_TAG_LENGTH = 128; // 128-bit authentication tag

    // Static keys for demonstration purposes (In production, load these from environment variables or KMS)
    private static final byte[] AES_KEY = new byte[] {
        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
        0x09, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16,
        0x17, 0x18, 0x19, 0x20, 0x21, 0x22, 0x23, 0x24,
        0x25, 0x26, 0x27, 0x28, 0x29, 0x30, 0x31, 0x32
    }; // 256-bit key

    private static final byte[] HMAC_KEY = new byte[] {
        0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57,
        0x58, 0x59, 0x60, 0x61, 0x62, 0x63, 0x64, 0x65,
        0x66, 0x67, 0x68, 0x69, 0x70, 0x71, 0x72, 0x73,
        0x74, 0x75, 0x76, 0x77, 0x78, 0x79, (byte) 0x80, (byte) 0x81
    }; // 256-bit key for HMAC-SHA256

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Encrypts a plaintext string using AES-256-GCM.
     * The output contains the 12-byte IV prepended to the ciphertext.
     */
    public byte[] encrypt(String plaintext) {
        if (plaintext == null) return null;
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv); // Generate a unique IV for every encryption

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            SecretKeySpec keySpec = new SecretKeySpec(AES_KEY, AES_ALGORITHM);

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Concatenate IV and ciphertext: [IV (12 bytes)][Ciphertext + Tag]
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);
            return byteBuffer.array();
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypts a byte array containing prepended IV followed by ciphertext.
     */
    public String decrypt(byte[] ivAndCiphertext) {
        if (ivAndCiphertext == null) return null;
        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(ivAndCiphertext);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);

            byte[] ciphertext = new byte[byteBuffer.remaining()];
            byteBuffer.get(ciphertext);

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            SecretKeySpec keySpec = new SecretKeySpec(AES_KEY, AES_ALGORITHM);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);
            byte[] decryptedText = cipher.doFinal(ciphertext);

            return new String(decryptedText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }

    /**
     * Generates a deterministic Blind Index for exact matches using HMAC-SHA256.
     */
    public String generateBlindIndex(String plaintext) {
        if (plaintext == null) return null;
        try {
            // Trim and convert to lowercase to make search case-insensitive and normalization-friendly
            String normalized = plaintext.trim().toLowerCase();

            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(HMAC_KEY, "HmacSHA256");
            sha256HMAC.init(keySpec);

            byte[] macBytes = sha256HMAC.doFinal(normalized.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(macBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate blind index", e);
        }
    }
}
