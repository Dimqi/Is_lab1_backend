package com.example.is_lab1.dto.house;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public record HouseRequest(
    @NotNull
    @NotBlank(message = "Имя не может быть пустым")
    String name,

    @Positive(message = "Год должен быть больше 0")
    @Max(value = 292, message = "Максимальный год 292")
    @NotNull(message = "Необходимо указать год")
    Long year,

    @Positive(message = "Количество квартир должно быть больше 0")
    @NotNull(message = "Необходимо указать количество этажей")
    Integer numberOfFlatsOnFloor
) {
}
