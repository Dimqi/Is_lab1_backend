package com.example.is_lab1.entities;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Coordinates {

    @NotNull(message = "Координата X не может быть null")
    private Double x; //Поле не может быть null

    private long y;
}