package com.beat.taskFlow.user;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.beat.taskFlow.common.exception.InvalidTokenException;
import com.beat.taskFlow.user.dto.requests.ResetPasswordRequest;
import com.beat.taskFlow.user.entity.concretes.PasswordResetToken;
import com.beat.taskFlow.user.entity.concretes.User;
import com.beat.taskFlow.user.repository.PasswordResetTokenRepository;
import com.beat.taskFlow.user.repository.UserRepository;
import com.beat.taskFlow.user.service.EmailService;
import com.beat.taskFlow.user.service.PasswordResetService;
import com.beat.taskFlow.user.service.RefreshTokenService;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    private PasswordResetService passwordResetService;
    private User user;
    private PasswordResetToken resetToken;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetService(
                userRepository,
                tokenRepository,
                emailService,
                passwordEncoder,
                refreshTokenService
        );

        user = new User();
        user.setId(1L);
        user.setEmail("test@taskflow.com");

        resetToken = PasswordResetToken.builder()
                .token("valid-token")
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();
    }

    @Test
    void resetPassword_Success_RevokesRefreshTokens() {
        ResetPasswordRequest request =
                new ResetPasswordRequest("valid-token", "NewPassword123!");

        when(tokenRepository.findByTokenAndUsedFalseAndExpiryDateAfter(
                any(String.class), any(LocalDateTime.class)))
                .thenReturn(Optional.of(resetToken));

        when(passwordEncoder.encode("NewPassword123!"))
                .thenReturn("encoded-password");

        when(userRepository.save(user)).thenReturn(user);
        when(tokenRepository.save(resetToken)).thenReturn(resetToken);

        passwordResetService.resetPassword(request);

        verify(passwordEncoder).encode("NewPassword123!");
        verify(userRepository).save(user);
        verify(refreshTokenService).revokeAllTokensForUser(user);
        verify(tokenRepository).save(resetToken);
    }

    @Test
    void resetPassword_InvalidToken_ThrowsException() {
        ResetPasswordRequest request =
                new ResetPasswordRequest("invalid-token", "NewPassword123!");

        when(tokenRepository.findByTokenAndUsedFalseAndExpiryDateAfter(
                any(String.class), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () ->
                passwordResetService.resetPassword(request)
        );
    }

    @Test
    void resetPassword_SetsTokenAsUsed() {
        ResetPasswordRequest request =
                new ResetPasswordRequest("valid-token", "NewPassword123!");

        when(tokenRepository.findByTokenAndUsedFalseAndExpiryDateAfter(
                any(String.class), any(LocalDateTime.class)))
                .thenReturn(Optional.of(resetToken));

        when(passwordEncoder.encode(any(String.class)))
                .thenReturn("encoded-password");

        passwordResetService.resetPassword(request);

        org.junit.jupiter.api.Assertions.assertTrue(resetToken.isUsed());
        verify(tokenRepository).save(resetToken);
    }
}