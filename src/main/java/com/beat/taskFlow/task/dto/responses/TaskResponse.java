package com.beat.taskFlow.task.dto.responses;

import com.beat.taskFlow.label.dto.responses.LabelResponse;
import com.beat.taskFlow.task.entity.enums.Priority;
import com.beat.taskFlow.task.entity.enums.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        Priority priority,
        LocalDate dueDate,
        Long projectId,
        Integer estimatedHours,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long assigneeId,
        String assigneeName,
        Long parentTaskId,
        List<LabelResponse> labels
) {}