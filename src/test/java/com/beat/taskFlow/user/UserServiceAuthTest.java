package com.beat.taskFlow.user;

import com.beat.taskFlow.common.exception.AlreadyExistsException;
import com.beat.taskFlow.user.dto.requests.LoginRequest;
import com.beat.taskFlow.user.dto.requests.RegisterRequest;
import com.beat.taskFlow.user.entity.concretes.User;
import com.beat.taskFlow.user.repository.UserRepository;
import com.beat.taskFlow.user.service.JwtService;
import com.beat.taskFlow.user.service.RefreshTokenService;
import com.beat.taskFlow.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceAuthTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private RefreshTokenService refreshTokenService;

    @InjectMocks private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().email("u@test.com").failedAttempt(2).build();
        user.setId(1L);
    }

    @Test
    void register_duplicateEmail_throwsAlreadyExists() {
        RegisterRequest req = new RegisterRequest("Ad", "dup@test.com", "Sifre123");
        when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(req))
                .isInstanceOf(AlreadyExistsException.class);
    }

    @Test
    void login_wrongPassword_locksAccountAfterThreeAttempts() {
        LoginRequest req = new LoginRequest("u@test.com", "wrong");

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> userService.login(req))
                .isInstanceOf(BadCredentialsException.class);

        verify(userRepository, atLeastOnce()).save(user);
        assertThat(user.getLockTime()).isNotNull();
    }
}