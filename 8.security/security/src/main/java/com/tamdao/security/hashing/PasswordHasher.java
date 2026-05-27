package com.tamdao.security.hashing;

public interface PasswordHasher {
    /**
     * Hash a plaintext password.
     * 
     * @param plaintext the raw password from the user
     * @return the encoded/hashed password
     */
    String hash(String plaintext);

    /**
     * Verify a plaintext password against a hashed password.
     * 
     * @param plaintext the raw password to check
     * @param hashed the stored hashed password to compare against
     * @return true if the password matches, false otherwise
     */
    boolean verify(String plaintext, String hashed);

    /**
     * Get the name of the hashing algorithm.
     * 
     * @return name of the algorithm
     */
    String getAlgorithmName();
}
