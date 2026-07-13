package com.beat.taskFlow.project.dto.requests;

import com.beat.taskFlow.project.entity.enums.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(
    @NotBlank(message = "Proje ismi boş bırakılamaz")
    @Size(max = 150, message = "Proje ismi en fazla 150 karakter olabilir")
    String name,
    
    String description,
    
    @NotNull(message = "Proje durumu boş bırakılamaz")
    ProjectStatus status
) {}