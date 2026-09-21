package com.example.is_lab1.dto.flat;

import com.example.is_lab1.dto.CoordinatesDto;
import com.example.is_lab1.entities.Furnish;
import com.example.is_lab1.entities.Transport;
import com.example.is_lab1.entities.View;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record FlatRequest(
        @NotNull(message = "Имя не может быть null")
        @NotBlank(message = "Строка не может быть пустой")
        String name,

        @NotNull(message = "Координаты обязательны")
        @Valid
        CoordinatesDto coordinates,


        @Min(value = 1, message = "Площадь должна быть больше 0")
        @Max(value = 763, message = "Максимальная площадь 763")
        int area,

        @Min(value = 1, message = "Цена должна быть больше 0")
        int price,

        Boolean balcony,

        @Positive(message = "Время до метро должно быть больше 0")
        Float timeToMetroOnFoot,

        @Positive(message = "Количество комнат должно быть больше 0")
        Integer numberOfRooms,

        Furnish furnish,
        View view,
        Transport transport,

        Long houseId
) {
}
