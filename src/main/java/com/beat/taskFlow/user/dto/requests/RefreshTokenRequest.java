package com.beat.taskFlow.user.dto.requests;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
    @NotBlank(message = "Refresh token boş bırakılamaz")
    String refreshToken
) {}