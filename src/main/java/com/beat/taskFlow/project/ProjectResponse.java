package com.beat.taskFlow.project;

import java.time.LocalDateTime;

public record ProjectResponse(
    Long id,
    String name,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}