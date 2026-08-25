package com.beat.taskFlow.task.dto.requests;

import jakarta.validation.constraints.Size;

public record UpdateCheckItemRequest(
        @Size(max = 200, message = "Başlık en fazla 200 karakter olabilir")
        String title,
        Boolean isCompleted
) {}