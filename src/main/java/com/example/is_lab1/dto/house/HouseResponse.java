package com.example.is_lab1.dto.house;

import com.example.is_lab1.entities.House;
public record HouseResponse(
        Long id,
        String name,
        Long year,
        Integer numberOfFlatsOnFloor

) {
    public static HouseResponse fromEntity(House entity){
        return new HouseResponse(
                entity.getId(),
                entity.getName(),
                entity.getYear(),
                entity.getNumberOfFlatsOnFloor());
    }


}
