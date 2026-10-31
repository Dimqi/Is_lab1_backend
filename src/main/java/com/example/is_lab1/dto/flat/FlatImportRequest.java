package com.example.is_lab1.dto.flat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record FlatImportRequest(
        @NotEmpty
        List<@Valid FlatRequest> flatRequests
) {
}
