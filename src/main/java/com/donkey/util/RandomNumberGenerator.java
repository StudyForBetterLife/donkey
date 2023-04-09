package com.donkey.util;

import lombok.NoArgsConstructor;

import java.security.SecureRandom;
import java.util.Base64;

@NoArgsConstructor
public class RandomNumberGenerator {
    private final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    public String generateTokenForPassword() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[10];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    public String generateTokenForEmail() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[30];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
}
