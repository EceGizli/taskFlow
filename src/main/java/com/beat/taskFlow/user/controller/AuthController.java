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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Kimlik doğrulama ve kullanıcı yönetimi uçları")
public class AuthController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    @Operation(summary = "Kullanıcı kaydı", description = "Sisteme yeni bir kullanıcı kaydeder.")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request) {

        return userService.register(request);
    }

    @Operation(summary = "Kullanıcı girişi", description = "E-posta ve şifre ile giriş yaparak Access ve Refresh Token alır.")
    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {

        return userService.login(request);
    }

    @Operation(summary = "Token yenile", description = "Refresh token kullanarak yeni bir Access Token üretir.")
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        return ResponseEntity.ok(userService.refreshToken(request));
    }

    @Operation(summary = "Mevcut kullanıcı bilgisi", description = "Giriş yapmış olan kullanıcının profil bilgilerini getirir.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<MeResponse> getMe(Authentication authentication) {
        return ResponseEntity.ok(userService.getCurrentUser(authentication.getName()));
    }

    @Operation(summary = "Şifremi unuttum", description = "Kullanıcının e-posta adresine şifre sıfırlama talimatları gönderir.")
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        passwordResetService.processForgotPassword(request);

        return ResponseEntity.ok(
                "Eğer e-posta adresi sistemimizde kayıtlıysa, şifre sıfırlama talimatları gönderilmiştir."
        );
    }

    @Operation(summary = "Şifre sıfırla", description = "Gönderilen token ile kullanıcının şifresini yeniler.")
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        passwordResetService.resetPassword(request);

        return ResponseEntity.ok(
                "Şifreniz başarıyla güncellendi. Yeni şifrenizle giriş yapabilirsiniz."
        );
    }
}