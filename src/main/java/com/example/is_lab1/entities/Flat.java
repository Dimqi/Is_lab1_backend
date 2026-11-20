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
    @SequenceGenerator(name = "flat_seq", sequenceName = "flat_seq", allocationSize = 1)
    private Long id; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически

    @Column(nullable = false)
    private String name; //Поле не может быть null, Строка не может быть пустой

    @Column(name = "creation_date", nullable = false, updatable = false)
    private java.time.LocalDate creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически

    @Column(nullable = false)
    private int area;

    @Column(nullable = false)
    private int price;

    @Column
    private Boolean balcony;

    @Column(name = "time_to_metro_on_foot")
    private Float timeToMetroOnFoot;

    @Column(name = "number_of_rooms")
    private Integer numberOfRooms;

    @Enumerated(EnumType.STRING)
    @Column(name = "furnish")
    private Furnish furnish;

    @Enumerated(EnumType.STRING)
    @Column(name = "view")
    private View view;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport")
    private Transport transport;

    @ManyToOne(optional = false)
    @JoinColumn(name = "house_id", nullable = false)
    private House house;
}