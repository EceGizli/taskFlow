package com.beat.taskFlow.user.dto.responses;

import com.beat.taskFlow.user.entity.enums.Role;
import java.time.LocalDateTime;

public record MeResponse(
    Long id,
    String name,
    String email,
    Role role,
    LocalDateTime createdAt
) {}