package com.beat.taskFlow.user.controller;

import com.beat.taskFlow.user.dto.requests.ForgotPasswordRequest;
import com.beat.taskFlow.user.dto.requests.LoginRequest;
import com.beat.taskFlow.user.dto.requests.RefreshTokenRequest;
import com.beat.taskFlow.user.dto.requests.RegisterRequest;
import com.beat.taskFlow.user.dto.requests.ResetPasswordRequest;
import com.beat.taskFlow.user.dto.responses.LoginResponse;
import com.beat.taskFlow.user.dto.responses.MeResponse;
import com.beat.taskFlow.user.dto.responses.RefreshTokenResponse;
import com.beat.taskFlow.user.dto.responses.RegisterResponse;
import com.beat.taskFlow.user.service.PasswordResetService;
import com.beat.taskFlow.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request) {

        return userService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {

        return userService.login(request);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        return ResponseEntity.ok(userService.refreshToken(request));
    }    
    
    @GetMapping("/me")
    public ResponseEntity<MeResponse> getMe(Authentication authentication) {
        return ResponseEntity.ok(userService.getCurrentUser(authentication.getName()));
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        passwordResetService.processForgotPassword(request);

        return ResponseEntity.ok(
                "Eğer e-posta adresi sistemimizde kayıtlıysa, şifre sıfırlama talimatları gönderilmiştir."
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        passwordResetService.resetPassword(request);

        return ResponseEntity.ok(
                "Şifreniz başarıyla güncellendi. Yeni şifrenizle giriş yapabilirsiniz."
        );
    }
}