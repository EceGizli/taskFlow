package com.beat.taskFlow.user.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(

        @NotBlank(message = "Token boş bırakılamaz")
        String token,

        @NotBlank(message = "Yeni şifre boş bırakılamaz")
        @Size(
                min = 8,
                max = 100,
                message = "Şifre 8 ile 100 karakter arasında olmalıdır"
        )
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "Şifre en az bir büyük harf, bir küçük harf ve bir rakam içermelidir."
        )
        String newPassword

) {}