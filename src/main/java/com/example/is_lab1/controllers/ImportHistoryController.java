package com.example.is_lab1.controllers;

import com.example.is_lab1.dto.importHistory.ImportHistoryResponse;
import com.example.is_lab1.entities.User;
import com.example.is_lab1.services.ImportHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/importHistory")
@RequiredArgsConstructor
public class ImportHistoryController {

    private final ImportHistoryService importHistoryService;

    @GetMapping
    public Page<ImportHistoryResponse> getImportHistory(@AuthenticationPrincipal User user, Pageable pageable
    ) {
        return importHistoryService.getImportHistory(user, pageable);
    }
}