package com.beat.taskFlow.task.dto.requests;

import jakarta.validation.constraints.NotNull;

public record UpdateTaskAssigneeRequest(

        @NotNull(message = "Atanacak kullanıcı seçilmelidir.")
        Long assigneeId

) {
}