package com.example.is_lab1.dto.flat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record FlatImportRequest(
        @NotEmpty
        List<@NotNull  @Valid FlatRequest> flatRequests
) {
}
