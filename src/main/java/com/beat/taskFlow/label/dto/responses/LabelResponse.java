package com.beat.taskFlow.label.dto.responses;

import java.time.LocalDateTime;

public record LabelResponse(

        Long id,
        String name,
        String color,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {}