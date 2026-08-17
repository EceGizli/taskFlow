package com.beat.taskFlow.label.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLabelRequest(

        @NotBlank(message = "Etiket adı boş olamaz.")
        @Size(max = 50, message = "Etiket adı en fazla 50 karakter olabilir.")
        String name,

        @Size(max = 7, message = "Renk kodu en fazla 7 karakter olabilir.")
        String color

) {}