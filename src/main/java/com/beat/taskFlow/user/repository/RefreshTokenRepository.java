package com.beat.taskFlow.user.repository;

import com.beat.taskFlow.user.entity.concretes.RefreshToken;
import com.beat.taskFlow.user.entity.concretes.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);
    void deleteByUser(User user);
    List<RefreshToken> findAllByUserAndRevokedFalse(User user);
}