package com.beat.taskFlow.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import com.beat.taskFlow.common.exception.InvalidTokenException;
import com.beat.taskFlow.user.entity.concretes.RefreshToken;
import com.beat.taskFlow.user.entity.concretes.User;
import com.beat.taskFlow.user.repository.RefreshTokenRepository;
import com.beat.taskFlow.user.service.RefreshTokenService;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;
    private User user;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository);
        ReflectionTestUtils.setField(
                refreshTokenService,
                "refreshTokenDurationMs",
                604800000L
        );

        user = new User();
        user.setId(1L);
        user.setEmail("test@taskflow.com");
    }

    @Test
    void createRefreshToken_Success() {
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken(user);

        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertNotNull(result.getToken());
        assertFalse(result.isRevoked());

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void verifyExpiration_ValidToken_Success() {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token("valid-token")
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        RefreshToken result = refreshTokenService.verifyExpiration(token);

        assertEquals(token, result);
        assertFalse(token.isRevoked());
    }

    @Test
    void verifyExpiration_RevokedToken_ThrowsException() {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token("revoked-token")
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(true)
                .build();

        assertThrows(InvalidTokenException.class, () ->
                refreshTokenService.verifyExpiration(token)
        );

        verify(refreshTokenRepository).save(token);
    }

    @Test
    void verifyExpiration_ExpiredToken_ThrowsException() {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token("expired-token")
                .expiryDate(Instant.now().minusSeconds(60))
                .revoked(false)
                .build();

        assertThrows(InvalidTokenException.class, () ->
                refreshTokenService.verifyExpiration(token)
        );

        assertTrue(token.isRevoked());
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void rotateRefreshToken_Success() {
        RefreshToken oldToken = RefreshToken.builder()
                .user(user)
                .token("old-token")
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        RefreshToken newToken = RefreshToken.builder()
                .user(user)
                .token("new-token")
                .expiryDate(Instant.now().plusSeconds(7200))
                .revoked(false)
                .build();

        when(refreshTokenRepository.save(any(RefreshToken.class)))
        .thenReturn(oldToken, newToken);

        RefreshToken result =
                refreshTokenService.rotateRefreshToken(oldToken);

        assertNotNull(result);
        assertTrue(oldToken.isRevoked());
        assertEquals(user, result.getUser());

        verify(refreshTokenRepository, atLeast(2))
                .save(any(RefreshToken.class));
    }

    @Test
    void revokeAllTokensForUser_RevokesActiveTokens() {
        RefreshToken token1 = RefreshToken.builder()
                .user(user)
                .token("token-1")
                .revoked(false)
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();

        RefreshToken token2 = RefreshToken.builder()
                .user(user)
                .token("token-2")
                .revoked(false)
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();

        when(refreshTokenRepository.findAllByUserAndRevokedFalse(user))
                .thenReturn(List.of(token1, token2));

        refreshTokenService.revokeAllTokensForUser(user);

        assertTrue(token1.isRevoked());
        assertTrue(token2.isRevoked());
    }

    @Test
    void findByToken_TokenNotFound_ThrowsException() {
        when(refreshTokenRepository.findByToken("invalid-token"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () ->
                refreshTokenService.findByToken("invalid-token")
        );
    }
}