package com.example.is_lab1.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "flats")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Flat {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "flat_seq")
    @SequenceGenerator(name = "flat_seq", sequenceName = "flat_id_seq", allocationSize = 1)
    private Long id; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически

    @Column(nullable = false)
    private String name; //Поле не может быть null, Строка не может быть пустой

    @Embedded
    private Coordinates coordinates; //Поле не может быть null

    @Column(name = "creation_date", nullable = false, updatable = false)
    private java.time.LocalDate creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически


    @Column(nullable = false)
    private int area; //Максимальное значение поля: 763, Значение поля должно быть больше 0

    @Column(nullable = false)
    private int price; //Значение поля должно быть больше 0

    @Column
    private Boolean balcony; //Поле может быть null

    @Column(name = "time_to_metro_on_foot")
    private Float timeToMetroOnFoot; //Значение поля должно быть больше 0

    @Column(name = "number_of_rooms")
    private Integer numberOfRooms; //Значение поля должно быть больше 0

    @Enumerated(EnumType.STRING)
    @Column(name = "furnish")
    private Furnish furnish; //Поле может быть null

    @Enumerated(EnumType.STRING)
    @Column(name = "view")
    private View view; //Поле может быть null

    @Enumerated(EnumType.STRING)
    @Column(name = "transport")
    private Transport transport; //Поле может быть null

    @ManyToOne
    @JoinColumn(name = "house_id")
    private House house; //Поле может быть null
}