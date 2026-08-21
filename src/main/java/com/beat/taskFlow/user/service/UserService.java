package com.beat.taskFlow.user.service;

import com.beat.taskFlow.common.exception.AccountLockedException;
import com.beat.taskFlow.common.exception.AlreadyExistsException;
import com.beat.taskFlow.common.exception.NotFoundException;
import com.beat.taskFlow.user.dto.requests.LoginRequest;
import com.beat.taskFlow.user.dto.requests.RefreshTokenRequest;
import com.beat.taskFlow.user.dto.requests.RegisterRequest;
import com.beat.taskFlow.user.dto.responses.LoginResponse;
import com.beat.taskFlow.user.dto.responses.MeResponse;
import com.beat.taskFlow.user.dto.responses.RefreshTokenResponse;
import com.beat.taskFlow.user.dto.responses.RegisterResponse;
import com.beat.taskFlow.user.entity.concretes.RefreshToken;
import com.beat.taskFlow.user.entity.concretes.User;
import com.beat.taskFlow.user.entity.enums.Role;
import com.beat.taskFlow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new AlreadyExistsException("Bu e-posta adresi zaten kullanımda: " + request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_USER)
                .failedAttempt(0)
                .build();

        User savedUser = userRepository.save(user);
        return new RegisterResponse(savedUser.getId(), savedUser.getName(), savedUser.getEmail(), savedUser.getRole());
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Geçersiz e-posta veya şifre"));

        if (user.getLockTime() != null) {
            if (user.getLockTime().isAfter(LocalDateTime.now())) {
                throw new AccountLockedException("Hesabınız çok fazla hatalı giriş nedeniyle kilitlenmiştir.");
            } else {
                user.setLockTime(null);
                user.setFailedAttempt(0);
                userRepository.save(user);
            }
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException ex) {
            int attempts = user.getFailedAttempt() + 1;
            user.setFailedAttempt(attempts);
            if (attempts >= 3) {
                user.setLockTime(LocalDateTime.now().plusMinutes(15));
            }
            userRepository.save(user);
            throw new BadCredentialsException("Geçersiz e-posta veya şifre");
        }

        user.setFailedAttempt(0);
        user.setLockTime(null);
        userRepository.save(user);

        String accessToken = jwtService.generateToken(user);
        refreshTokenService.revokeAllTokensForUser(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new LoginResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken oldToken = refreshTokenService.findByToken(request.refreshToken());
        RefreshToken newToken = refreshTokenService.rotateRefreshToken(oldToken);

        String newAccessToken = jwtService.generateToken(newToken.getUser());

        return new RefreshTokenResponse(newAccessToken, newToken.getToken(), "Bearer");
    }

    public MeResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı"));
        return new MeResponse(
                user.getId(), 
                user.getName(), 
                user.getEmail(), 
                user.getRole(), 
                user.getCreatedAt()
        );
    }
}