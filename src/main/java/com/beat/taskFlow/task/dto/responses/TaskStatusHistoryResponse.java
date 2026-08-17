package com.beat.taskFlow.task.dto.responses;

import com.beat.taskFlow.task.entity.enums.TaskStatus;

import java.time.LocalDateTime;

public record TaskStatusHistoryResponse(
        Long id,
        Long taskId,
        TaskStatus status,
        LocalDateTime createdAt
) {}