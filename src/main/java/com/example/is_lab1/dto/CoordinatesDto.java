package com.example.is_lab1.dto;

import com.example.is_lab1.entities.Coordinates;
import jakarta.validation.constraints.NotNull;

public record CoordinatesDto(
        @NotNull(message = "Координата X не может быть null")
        Double x,
        long y) {
    public static CoordinatesDto fromEntity(Coordinates entity){
        return new CoordinatesDto(
                entity.getX(),
                entity.getY()
        );

    }


}
