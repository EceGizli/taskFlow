package com.beat.taskFlow.task.dto.requests;

import com.beat.taskFlow.task.entity.enums.TaskStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BulkUpdateStatusRequest(
        @NotEmpty(message = "Güncellenecek görev ID listesi boş olamaz")
        List<Long> taskIds,

        @NotNull(message = "Yeni durum seçilmelidir")
        TaskStatus status
) {}