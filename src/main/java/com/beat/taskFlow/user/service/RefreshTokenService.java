package com.beat.taskFlow.user.service;

import com.beat.taskFlow.common.exception.InvalidTokenException;
import com.beat.taskFlow.user.entity.concretes.RefreshToken;
import com.beat.taskFlow.user.entity.concretes.User;
import com.beat.taskFlow.user.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.jwt.refresh-expiration-ms:604800000}") 
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken oldToken) {
        verifyExpiration(oldToken);
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);
        return createRefreshToken(oldToken.getUser());
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isRevoked() || token.getExpiryDate().compareTo(Instant.now()) < 0) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            throw new InvalidTokenException("Refresh token süresi dolmuş veya geçersiz.");
        }
        return token;
    }

    @Transactional
    public void revokeAllTokensForUser(User user) {
        refreshTokenRepository.findAllByUserAndRevokedFalse(user)
                .forEach(t -> t.setRevoked(true));
    }

    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Refresh token bulunamadı."));
    }
}