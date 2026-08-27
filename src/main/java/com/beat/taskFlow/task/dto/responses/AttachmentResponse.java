package com.beat.taskFlow.task.dto.responses;

import java.time.LocalDateTime;

public record AttachmentResponse(
        Long id,
        String fileName,
        String fileType,
        long fileSize,
        Long taskId,
        String uploadedByName,
        LocalDateTime createdAt
) {}