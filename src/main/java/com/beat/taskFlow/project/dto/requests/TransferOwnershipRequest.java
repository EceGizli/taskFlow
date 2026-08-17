package com.beat.taskFlow.project.dto.requests;

import jakarta.validation.constraints.NotNull;

public record TransferOwnershipRequest(
    @NotNull(message = "Yeni sahip ID'si boş olamaz")
    Long newOwnerId
) {}