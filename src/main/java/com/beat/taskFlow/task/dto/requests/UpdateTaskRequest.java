package com.beat.taskFlow.task.dto.requests;

import com.beat.taskFlow.task.entity.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateTaskRequest(

    @NotBlank(message = "Görev başlığı boş bırakılamaz")
    @Size(max = 200, message = "Görev başlığı en fazla 200 karakter olabilir")
    String title,

    String description,

    @NotNull(message = "Öncelik seçilmelidir")
    Priority priority,

    LocalDate dueDate

) {}