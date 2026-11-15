package com.example.is_lab1.dto.importHistory;

import com.example.is_lab1.entities.importHistory.ImportHistory;
import com.example.is_lab1.entities.importHistory.ImportType;

import java.time.LocalDateTime;

public record ImportHistoryResponse(
        Long id,
        String status,
        ImportType importType,
        String username,
        Integer recordsCount,
        LocalDateTime startedAt
) {
    public static ImportHistoryResponse fromEntity(ImportHistory importHistory) {
        return new ImportHistoryResponse(
                importHistory.getId(),
                importHistory.getStatus().name(),
                importHistory.getImportType(),
                importHistory.getUser().getUsername(),
                importHistory.getRecordsCount(),
                importHistory.getStartedAt()
        );
    }
}