package com.beat.taskFlow.user.dto.responses;

public record LoginResponse(
    String token,
    String refreshToken,
    String tokenType,
    Long userId,
    String name,
    String email
) {}