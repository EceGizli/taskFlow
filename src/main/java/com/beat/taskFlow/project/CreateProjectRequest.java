package com.beat.taskFlow.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(
    @NotBlank(message = "Proje adı boş bırakılamaz")
    @Size(max = 150, message = "Proje adı en fazla 150 karakter olabilir")
    String name,
    
    String description
) {}