package com.beat.taskFlow.user.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(

        @NotBlank(message = "Token boş bırakılamaz")
        String token,

        @NotBlank(message = "Yeni şifre boş bırakılamaz")
        @Size(
                min = 8,
                max = 64,
                message = "Şifre en az 8, en fazla 64 karakter olmalıdır"
        )
        String newPassword

) {
}