package com.beat.taskFlow.user.service;

import com.beat.taskFlow.common.exception.InvalidTokenException;
import com.beat.taskFlow.user.dto.requests.ForgotPasswordRequest;
import com.beat.taskFlow.user.dto.requests.ResetPasswordRequest;
import com.beat.taskFlow.user.entity.concretes.PasswordResetToken;
import com.beat.taskFlow.user.entity.concretes.User;
import com.beat.taskFlow.user.repository.PasswordResetTokenRepository;
import com.beat.taskFlow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.beat.taskFlow.user.service.RefreshTokenService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.security.password-reset-expiration:15}")
    private long expirationMinutes;

    @Transactional
    public void processForgotPassword(ForgotPasswordRequest request) {

        Optional<User> userOptional =
                userRepository.findByEmail(request.email());


        if (userOptional.isEmpty()) {
            return;
        }

        User user = userOptional.get();

        List<PasswordResetToken> activeTokens =
                tokenRepository.findByUserAndUsedFalse(user);

        for (PasswordResetToken oldToken : activeTokens) {
            oldToken.setUsed(true);
        }

        tokenRepository.saveAll(activeTokens);

        String token = UUID.randomUUID()
                .toString()
                .replace("-", "");

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(
                        LocalDateTime.now()
                                .plusMinutes(expirationMinutes)
                )
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(
                user.getEmail(),
                token,
                expirationMinutes
        );
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        PasswordResetToken resetToken =
                tokenRepository
                        .findByTokenAndUsedFalseAndExpiryDateAfter(
                                request.token(),
                                LocalDateTime.now()
                        )
                        .orElseThrow(() ->
                                new InvalidTokenException(
                                        "Geçersiz, kullanılmış veya süresi dolmuş şifre sıfırlama token'ı."
                                )
                        );

        User user = resetToken.getUser();

        user.setPassword(
                passwordEncoder.encode(request.newPassword())
        );

        userRepository.save(user);
        
        refreshTokenService.revokeAllTokensForUser(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
}