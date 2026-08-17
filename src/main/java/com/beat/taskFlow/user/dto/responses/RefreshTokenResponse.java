package com.beat.taskFlow.user.dto.responses;

public record RefreshTokenResponse(
    String accessToken,
    String refreshToken,
    String tokenType
) {}