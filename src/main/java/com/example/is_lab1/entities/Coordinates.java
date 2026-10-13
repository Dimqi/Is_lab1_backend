package com.example.is_lab1.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import javax.annotation.processing.Generated;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "coordinates")
public class Coordinates {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "coordinates_seq")
    @SequenceGenerator(name = "coordinates_seq", sequenceName = "coordinates_seq", allocationSize = 1)
    Long id;

    @NotNull(message = "Координата X не может быть null")
    private Double x; //Поле не может быть null

    private Long y;

}