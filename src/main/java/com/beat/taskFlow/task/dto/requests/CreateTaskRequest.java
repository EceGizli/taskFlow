package com.beat.taskFlow.task.dto.requests;

import com.beat.taskFlow.task.entity.enums.Priority;
import com.beat.taskFlow.task.entity.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateTaskRequest(
        @NotBlank @Size(max = 200) String title,
        String description,
        TaskStatus status,
        Priority priority,
        LocalDate dueDate,
        Integer estimatedHours,
        Long assigneeId,
        Long parentTaskId
) {}