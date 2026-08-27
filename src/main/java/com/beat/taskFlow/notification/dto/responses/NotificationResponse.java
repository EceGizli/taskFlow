package com.beat.taskFlow.notification.dto.responses;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String title,
        String message,
        boolean isRead,
        LocalDateTime createdAt
) {}