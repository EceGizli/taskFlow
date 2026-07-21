package com.beat.taskFlow.user.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "E-posta boş bırakılamaz")
        @Email(message = "Geçerli bir e-posta giriniz")
        String email,

        @NotBlank(message = "Şifre boş bırakılamaz")
        String password

) {}