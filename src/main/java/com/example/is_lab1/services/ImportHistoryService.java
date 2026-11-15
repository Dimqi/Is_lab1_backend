package com.example.is_lab1.services;

import com.example.is_lab1.dto.importHistory.ImportHistoryResponse;
import com.example.is_lab1.entities.Role;
import com.example.is_lab1.entities.User;
import com.example.is_lab1.entities.importHistory.ImportHistory;
import com.example.is_lab1.entities.importHistory.ImportStatus;
import com.example.is_lab1.entities.importHistory.ImportType;
import com.example.is_lab1.repositories.ImportHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ImportHistoryService {
    private final ImportHistoryRepository importHistoryRepository;

    @Transactional(readOnly = true)
    public Page<ImportHistoryResponse> getImportHistory(User user, Pageable pageable) {
        if (user.getRole() == Role.ADMIN) {
            return importHistoryRepository.findAllByOrderByStartedAtDesc(pageable)
                    .map(ImportHistoryResponse::fromEntity);
        }

        return importHistoryRepository
                .findAllByUser_IdOrderByStartedAtDesc(user.getId(), pageable)
                .map(ImportHistoryResponse::fromEntity);
    }



    @Transactional
    public void saveSuccessfullyImport(User user, ImportType importType, ImportStatus status, int recordsCount, LocalDateTime startedAt
    ) {
        ImportHistory history = new ImportHistory();
        history.setUser(user);
        history.setImportType(importType);
        history.setStatus(status);
        history.setRecordsCount(recordsCount);
        history.setStartedAt(startedAt);

        importHistoryRepository.save(history);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailedImport(
            User user,
            ImportType importType,
            LocalDateTime startedAt
    ) {
        ImportHistory history = new ImportHistory();

        history.setUser(user);
        history.setImportType(importType);
        history.setStatus(ImportStatus.FAILED);
        history.setRecordsCount(null);
        history.setStartedAt(startedAt);

        importHistoryRepository.save(history);
    }



}
