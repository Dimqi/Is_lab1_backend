package com.example.is_lab1.dto.house;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record HouseImportRequest(
        @NotEmpty
        List<@NotNull @Valid HouseRequest> houseRequests
) {
}
