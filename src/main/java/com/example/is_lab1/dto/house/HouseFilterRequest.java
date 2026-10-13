package com.example.is_lab1.dto.house;

public record HouseFilterRequest(
        Integer yearMin,
        Integer yearMax,
        String name,
        Integer numberOfFlatsOnFloorMin,
        Integer numberOfFlatsOnFloorMax
) {}