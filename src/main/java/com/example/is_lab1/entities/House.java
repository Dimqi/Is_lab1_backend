package com.example.is_lab1.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;


import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "house")
public class House {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "house_seq")
    @SequenceGenerator(name = "house_seq", sequenceName = "house_seq", allocationSize = 1)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String name;

    @Positive
    @Max(value = 292)
    private Long year;

    @Positive
    private Integer numberOfFlatsOnFloor;

    @OneToMany(mappedBy = "house")
    private List<Flat> flats;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "coordinates_id")
    private Coordinates coordinates;
}