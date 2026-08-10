package com.beat.taskFlow.user.repository;

import com.beat.taskFlow.user.entity.concretes.PasswordResetToken;
import com.beat.taskFlow.user.entity.concretes.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenAndUsedFalseAndExpiryDateAfter(
            String token,
            LocalDateTime now
    );

    List<PasswordResetToken> findByUserAndUsedFalse(User user);
}