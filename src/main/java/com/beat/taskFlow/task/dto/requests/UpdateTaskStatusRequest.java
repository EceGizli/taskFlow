package com.beat.taskFlow.task.dto.requests;

import com.beat.taskFlow.task.entity.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(

        @NotNull(message = "Görev durumu seçilmelidir")
        TaskStatus status

) {}