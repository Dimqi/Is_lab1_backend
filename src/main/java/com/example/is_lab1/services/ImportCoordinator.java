package com.example.is_lab1.services;

import com.example.is_lab1.dto.flat.FlatResponse;
import com.example.is_lab1.dto.house.HouseResponse;
import com.example.is_lab1.entities.User;
import com.example.is_lab1.entities.importHistory.ImportType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportCoordinator {

    private final HouseService houseService;
    private final ImportHistoryService importHistoryService;
    private final FlatService flatService;

    public List<HouseResponse> importHousesFromFile(
            User user,
            MultipartFile file
    ) throws IOException {

        LocalDateTime startedAt = LocalDateTime.now();

        try {
            return houseService.importHousesFromFile(user, file, startedAt);
        } catch (IOException | RuntimeException e) {

            importHistoryService.saveFailedImport(user, ImportType.HOUSE, startedAt);

            throw e;
        }
    }


    public List<FlatResponse> importFlatsFromFile(
            User user,
            MultipartFile file
    ) throws IOException {

        LocalDateTime startedAt = LocalDateTime.now();

        try {
            return flatService.importFlatsFromFile(user, file, startedAt);
        } catch (IOException | RuntimeException e) {
            importHistoryService.saveFailedImport(user, ImportType.FLAT, startedAt);
            throw e;
        }
    }
}