package com.beat.taskFlow.comment.dto.responses;

import java.time.LocalDateTime;

public record CommentResponse(

        Long id,
        String content,

        Long taskId,

        Long authorId,
        String authorName,

        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {}