package com.example.is_lab1.dto.flat;

import com.example.is_lab1.dto.CoordinatesDto;
import com.example.is_lab1.entities.Flat;
import com.example.is_lab1.entities.Furnish;
import com.example.is_lab1.entities.Transport;
import com.example.is_lab1.entities.View;

import java.time.LocalDate;

public record FlatResponse(
        Long id,
        String name,
        CoordinatesDto coordinates,
        LocalDate creationDate,
        int area,
        int price,
        Boolean balcony,
        Float timeToMetroOnFoot,
        Integer numberOfRooms,
        Furnish furnish,
        View view,
        Transport transport,
        Long houseId
) {

    public static FlatResponse fromEntity(Flat entity) {
        return new FlatResponse(
                entity.getId(),
                entity.getName(),
                CoordinatesDto.fromEntity(entity.getCoordinates()),
                entity.getCreationDate(),
                entity.getArea(),
                entity.getPrice(),
                entity.getBalcony(),
                entity.getTimeToMetroOnFoot(),
                entity.getNumberOfRooms(),
                entity.getFurnish(),
                entity.getView(),
                entity.getTransport(),
                entity.getHouse() != null ? entity.getHouse().getId() : null
        );
    }

}
