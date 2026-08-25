package com.beat.taskFlow.task.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCheckItemRequest(
        @NotBlank(message = "Kontrol maddesi başlığı boş olamaz")
        @Size(max = 200, message = "Başlık en fazla 200 karakter olabilir")
        String title
) {}