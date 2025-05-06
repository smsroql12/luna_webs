package com.example.luna.service;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordResetTokenService {
    private final Map<String, ResetToken> tokenStore = new ConcurrentHashMap<>();

    public String createToken(String email) {
        String token = UUID.randomUUID().toString();
        ResetToken resetToken = new ResetToken(email, LocalDateTime.now().plusHours(2));
        tokenStore.put(token, resetToken);
        return token;
    }

    public String getEmailIfValid(String token) {
        ResetToken resetToken = tokenStore.get(token);
        if (resetToken == null || resetToken.expired()) return null;
        return resetToken.getEmail();
    }

    public void invalidateToken(String token) {
        tokenStore.remove(token);
    }

    // 내부 클래스
    private static class ResetToken {
        @Getter
        private final String email;
        private final LocalDateTime expiry;

        ResetToken(String email, LocalDateTime expiry) {
            this.email = email;
            this.expiry = expiry;
        }

        public boolean expired() {
            return LocalDateTime.now().isAfter(expiry);
        }

    }
}