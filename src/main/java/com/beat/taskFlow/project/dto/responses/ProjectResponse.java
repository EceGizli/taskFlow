package com.beat.taskFlow.project.dto.responses;

import com.beat.taskFlow.project.entity.enums.ProjectStatus;
import java.time.LocalDateTime;

public record ProjectResponse(
    Long id,
    String name,
    String description,
    ProjectStatus status,
    LocalDateTime createdAt
) {}