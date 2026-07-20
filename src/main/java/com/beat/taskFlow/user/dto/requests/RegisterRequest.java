package com.beat.taskFlow.user.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "İsim boş bırakılamaz")
        @Size(max = 100, message = "İsim en fazla 100 karakter olabilir")
        String name,

        @NotBlank(message = "E-posta boş bırakılamaz")
        @Email(message = "Geçerli bir e-posta adresi giriniz")
        @Size(max = 150, message = "E-posta en fazla 150 karakter olabilir")
        String email,

        @NotBlank(message = "Şifre boş bırakılamaz")
        @Size(min = 6, max = 100, message = "Şifre 6 ile 100 karakter arasında olmalıdır")
        String password

) {}