package com.example.is_lab1.services;

import com.example.is_lab1.dto.ChangeEvent;
import com.example.is_lab1.dto.CoordinatesDto;
import com.example.is_lab1.dto.house.HouseFilterRequest;
import com.example.is_lab1.dto.house.HouseImportRequest;
import com.example.is_lab1.dto.house.HouseRequest;
import com.example.is_lab1.dto.house.HouseResponse;
import com.example.is_lab1.entities.Coordinates;
import com.example.is_lab1.entities.House;
import com.example.is_lab1.exceptions.CoordinatesAlreadyExistException;
import com.example.is_lab1.exceptions.HouseNotFoundException;
import com.example.is_lab1.repositories.CoordinatesRepository;
import com.example.is_lab1.repositories.FlatRepository;
import com.example.is_lab1.repositories.HouseRepository;
import com.example.is_lab1.repositories.specifications.CoordinatesSpecifications;
import com.example.is_lab1.repositories.specifications.HouseSpecifications;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HouseService {

    private final HouseRepository houseRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final FlatRepository flatRepository;
    private final CoordinatesRepository coordinatesRepository;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Retryable(
            retryFor = Exception.class,
            exceptionExpression = "@serializationFailure.isSerializationFailure(#root)",
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public HouseResponse createHouse(HouseRequest request) {

        if (coordinatesRepository.existsByXAndY(request.coordinates().x(), request.coordinates().y()
        )) {
            throw new CoordinatesAlreadyExistException(
                    "house with same coordinates already exist"
            );
        }

        House house = House.builder()
                .name(request.name())
                .year(request.year())
                .numberOfFlatsOnFloor(request.numberOfFlatsOnFloor())
                .coordinates(
                        Coordinates.builder()
                                .x(request.coordinates().x())
                                .y(request.coordinates().y())
                                .build()
                )
                .build();

        houseRepository.save(house);

        HouseResponse responseDto = HouseResponse.fromEntity(house);

        sendWsEventAfterCommit(
                ChangeEvent.of(
                        ChangeEvent.EntityType.HOUSE,
                        ChangeEvent.ActionType.CREATE,
                        responseDto.id(),
                        responseDto
                )
        );

        return responseDto;
    }

    @Transactional(readOnly = true)
    public Page<HouseResponse> getAllHouses(
            HouseFilterRequest filter,
            Pageable pageable
    ) {
        Specification<House> spec = HouseSpecifications.withFilter(filter);

        Page<House> housesPage = houseRepository.findAll(spec, pageable);

        return housesPage.map(HouseResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public HouseResponse getHouseById(Long id) {

        House house = houseRepository.findById(id)
                .orElseThrow(() ->
                        new HouseNotFoundException("House not found id =" + id)
                );

        return HouseResponse.fromEntity(house);
    }

    @Retryable(
            retryFor = Exception.class,
            exceptionExpression = "@serializationFailure.isSerializationFailure(#root)",
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteHouseById(Long id, Long newHouseId) {

        House houseToDelete = houseRepository.findById(id)
                .orElseThrow(() ->
                        new HouseNotFoundException("House not found")
                );

        if (newHouseId != null) {

            House newHouse = houseRepository.findById(newHouseId)
                    .orElseThrow(() ->
                            new HouseNotFoundException(
                                    "New house not found id = " + newHouseId
                            )
                    );

            flatRepository.updateHouseForFlats(id, newHouse);
        }

        houseRepository.delete(houseToDelete);

        sendWsEventAfterCommit(
                ChangeEvent.of(
                        ChangeEvent.EntityType.HOUSE,
                        ChangeEvent.ActionType.DELETE,
                        id
                )
        );
    }

    @Retryable(
            retryFor = Exception.class,
            exceptionExpression = "@serializationFailure.isSerializationFailure(#root)",
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public HouseResponse updateHouse(HouseRequest request, Long id) {

        House house = houseRepository.findById(id)
                .orElseThrow(() ->
                        new HouseNotFoundException("House not found id = " + id)
                );

        if (coordinatesRepository.existsByXAndY(
                request.coordinates().x(),
                request.coordinates().y()
        )) {
            throw new RuntimeException(
                    "house with same coordinates already exist"
            );
        }

        house.setName(request.name());
        house.setYear(request.year());
        house.setNumberOfFlatsOnFloor(request.numberOfFlatsOnFloor());

        house.setCoordinates(
                Coordinates.builder()
                        .x(request.coordinates().x())
                        .y(request.coordinates().y())
                        .build()
        );

        HouseResponse responseDto = HouseResponse.fromEntity(house);

        sendWsEventAfterCommit(
                ChangeEvent.of(
                        ChangeEvent.EntityType.HOUSE,
                        ChangeEvent.ActionType.UPDATE,
                        responseDto.id(),
                        responseDto
                )
        );

        return responseDto;
    }

    @Retryable(
            retryFor = Exception.class,
            exceptionExpression = "@serializationFailure.isSerializationFailure(#root)",
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public List<HouseResponse> importHousesFromFile(MultipartFile file)
            throws IOException {

        HouseImportRequest request = objectMapper.readValue(
                file.getInputStream(),
                HouseImportRequest.class
        );

        Set<ConstraintViolation<HouseImportRequest>> violations =
                validator.validate(request);

        if (!violations.isEmpty()) {

            String errors = violations.stream()
                    .map(violation ->
                            violation.getPropertyPath() + ": " +
                                    violation.getMessage()
                    )
                    .collect(Collectors.joining("; "));

            throw new IllegalArgumentException(
                    "Ошибка валидации файла: " + errors
            );
        }

        Optional<CoordinatesDto> validFromFile =
                validateUniqueCoordinatesFromFile(request.houseRequests());

        if (validFromFile.isPresent()) {

            String duplicateCoordinates = validFromFile.get().toString();

            throw new RuntimeException(
                    "Файл содержит дома с повторяющимися координатами = " +
                            duplicateCoordinates
            );
        }

        List<CoordinatesDto> validFromDB =
                validateUniqueCoordinatesFromDB(request.houseRequests());

        if (!validFromDB.isEmpty()) {

            StringBuilder errorBuilder = new StringBuilder(
                    "В базе уже содержаться дома с координатами: \n"
            );

            for (CoordinatesDto coordinatesDto : validFromDB) {
                errorBuilder.append(coordinatesDto).append("\n");
            }

            throw new RuntimeException(errorBuilder.toString());
        }

        return importHouses(request.houseRequests());
    }

    private List<HouseResponse> importHouses(
            List<HouseRequest> houseRequests
    ) {

        List<House> houses = houseRequests.stream()
                .map(house -> House.builder()
                        .name(house.name())
                        .year(house.year())
                        .numberOfFlatsOnFloor(house.numberOfFlatsOnFloor())
                        .coordinates(
                                Coordinates.builder()
                                        .x(house.coordinates().x())
                                        .y(house.coordinates().y())
                                        .build()
                        )
                        .build()
                )
                .toList();

        return houseRepository.saveAll(houses)
                .stream()
                .map(HouseResponse::fromEntity)
                .toList();
    }

    private Optional<CoordinatesDto> validateUniqueCoordinatesFromFile(
            List<HouseRequest> houseRequests
    ) {

        Set<CoordinatesDto> uniqueCoordinates = new HashSet<>();

        for (HouseRequest houseReq : houseRequests) {

            CoordinatesDto coordinates = houseReq.coordinates();

            if (!uniqueCoordinates.add(coordinates)) {
                return Optional.of(coordinates);
            }
        }

        return Optional.empty();
    }

    private List<CoordinatesDto> validateUniqueCoordinatesFromDB(
            List<HouseRequest> houseRequests
    ) {

        Specification<Coordinates> spec =
                CoordinatesSpecifications.withCoordinatesUsedByHouse(houseRequests);

        return coordinatesRepository.findAll(spec)
                .stream()
                .map(CoordinatesDto::fromEntity)
                .toList();
    }

    private void sendWsEventAfterCommit(ChangeEvent<?> event) {

        if (TransactionSynchronizationManager.isActualTransactionActive()) {

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {

                        @Override
                        public void afterCommit() {
                            messagingTemplate.convertAndSend(
                                    "/topic/events",
                                    event
                            );
                        }
                    }
            );

        } else {
            messagingTemplate.convertAndSend(
                    "/topic/events",
                    event
            );
        }
    }
}