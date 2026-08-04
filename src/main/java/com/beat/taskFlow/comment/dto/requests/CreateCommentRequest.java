package com.beat.taskFlow.comment.dto.requests;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(

        @NotBlank(message = "Yorum boş olamaz.")
        String content

) {
}