package com.beat.taskFlow.user.service;

import com.beat.taskFlow.common.exception.AccountLockedException;
import com.beat.taskFlow.common.exception.AlreadyExistsException;
import com.beat.taskFlow.common.exception.NotFoundException;
import com.beat.taskFlow.user.dto.requests.LoginRequest;
import com.beat.taskFlow.user.dto.requests.RegisterRequest;
import com.beat.taskFlow.user.dto.responses.LoginResponse;
import com.beat.taskFlow.user.dto.responses.MeResponse;
import com.beat.taskFlow.user.dto.responses.RegisterResponse;
import com.beat.taskFlow.user.entity.concretes.User;
import com.beat.taskFlow.user.entity.enums.Role;
import com.beat.taskFlow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long LOCK_DURATION_MINUTES = 15;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new AlreadyExistsException("Bu e-posta adresi zaten kayıtlı.");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_USER)
                .build();

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı."));

        if (isAccountLocked(user)) {
            if (isLockExpired(user)) {
                unlock(user);
            } else {
                throw new AccountLockedException(
                        "Hesabınız 15 dakika boyunca kilitlenmiştir. Lütfen daha sonra tekrar deneyiniz."
                );
            }
        }

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );

        } catch (BadCredentialsException ex) {

            increaseFailedAttempts(user);

            if (user.getFailedAttempt() >= MAX_FAILED_ATTEMPTS) {
                lock(user);
                throw new AccountLockedException(
                        "3 kez hatalı giriş yaptığınız için hesabınız 15 dakika süreyle kilitlendi."
                );
            }

            throw ex;
        }

        resetFailedAttempts(user);

        String token = jwtService.generateToken(user);

        return new LoginResponse(token);
    }

    @Transactional(readOnly = true)
    public MeResponse me(Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı."));

        return new MeResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increaseFailedAttempts(User user) {
        user.setFailedAttempt(user.getFailedAttempt() + 1);
        userRepository.saveAndFlush(user);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetFailedAttempts(User user) {
        user.setFailedAttempt(0);
        user.setLockTime(null);
        userRepository.saveAndFlush(user);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void lock(User user) {
        user.setLockTime(LocalDateTime.now());
        userRepository.saveAndFlush(user);
    }

    private boolean isAccountLocked(User user) {
        return user.getLockTime() != null;
    }

    private boolean isLockExpired(User user) {
        return user.getLockTime()
                .plusMinutes(LOCK_DURATION_MINUTES)
                .isBefore(LocalDateTime.now());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void unlock(User user) {
        user.setFailedAttempt(0);
        user.setLockTime(null);
        userRepository.saveAndFlush(user);
    }
}