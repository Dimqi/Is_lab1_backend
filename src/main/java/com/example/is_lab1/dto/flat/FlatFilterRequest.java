package com.example.is_lab1.dto.flat;

import com.example.is_lab1.entities.*;
import java.time.LocalDate;

public record FlatFilterRequest(
        String name,

        LocalDate creationDateFrom,
        LocalDate creationDateTo,

        Integer areaMin,
        Integer areaMax,

        Integer priceMin,
        Integer priceMax,

        Boolean balcony,

        Float timeToMetroOnFootMin,
        Float timeToMetroOnFootMax,

        Integer numberOfRoomsMin,
        Integer numberOfRoomsMax,

        Furnish furnish,
        View view,
        Transport transport,

        Long houseId
) {
}