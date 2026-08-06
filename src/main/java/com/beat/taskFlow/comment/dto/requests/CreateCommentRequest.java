package com.beat.taskFlow.comment.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(

		@NotBlank(message = "Yorum boş olamaz.")
		@Size(max = 500, message = "Yorum en fazla 500 karakter olabilir.")
		String content
) {}