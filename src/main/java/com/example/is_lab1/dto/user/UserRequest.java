package com.example.is_lab1.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRequest(
        @NotBlank(message = "Имя обязательно")
        @NotNull(message = "Имя обязательно")
        String username,

        @NotBlank(message = "Пароль обязателен")
        @NotNull(message = "Пароль обязателен")
        String password
) {
}
