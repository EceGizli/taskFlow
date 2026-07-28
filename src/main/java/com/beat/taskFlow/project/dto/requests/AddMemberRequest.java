package com.beat.taskFlow.project.dto.requests;

import jakarta.validation.constraints.NotNull;

public record AddMemberRequest(
        @NotNull(message = "Kullanıcı ID boş bırakılamaz")
        Long userId
) {}