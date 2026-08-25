package com.beat.taskFlow.task.dto.responses;

import java.time.LocalDateTime;

public record CheckItemResponse(
        Long id,
        String title,
        boolean isCompleted,
        Long taskId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}