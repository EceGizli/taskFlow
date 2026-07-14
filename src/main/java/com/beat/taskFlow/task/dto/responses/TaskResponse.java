package com.beat.taskFlow.task.dto.responses;

import com.beat.taskFlow.task.entity.enums.Priority;
import com.beat.taskFlow.task.entity.enums.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
    Long id,
    String title,
    String description,
    TaskStatus status,
    Priority priority,
    LocalDate dueDate,
    Long projectId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}