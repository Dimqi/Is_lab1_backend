package com.example.is_lab1.dto.house;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record HouseImportRequest(
        @NotEmpty
        List<@Valid HouseRequest> houseRequests
) {
}
