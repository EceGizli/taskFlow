package com.beat.taskFlow.project.dto.requests;

import com.beat.taskFlow.project.entity.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record AddMemberRequest(
        @NotNull(message = "Kullanıcı ID boş olamaz")
        Long userId,

        ProjectRole role
) {}