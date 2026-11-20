package com.example.is_lab1.services;

import com.example.is_lab1.dto.ChangeEvent;
import com.example.is_lab1.dto.CoordinatesDto;
import com.example.is_lab1.dto.house.HouseFilterRequest;
import com.example.is_lab1.dto.house.HouseImportRequest;
import com.example.is_lab1.dto.house.HouseRequest;
import com.example.is_lab1.dto.house.HouseResponse;
import com.example.is_lab1.entities.Coordinates;
import com.example.is_lab1.entities.Flat;
import com.example.is_lab1.entities.House;
import com.example.is_lab1.entities.User;
import com.example.is_lab1.entities.importHistory.ImportStatus;
import com.example.is_lab1.entities.importHistory.ImportType;
import com.example.is_lab1.exceptions.CoordinatesAlreadyExistException;
import com.example.is_lab1.exceptions.DataConflictException;
import com.example.is_lab1.exceptions.HouseNotFoundException;
import com.example.is_lab1.exceptions.ImportValidationException;
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
import java.time.LocalDateTime;
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
    private final ImportHistoryService importHistoryService;

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

            if (id.equals(newHouseId)) {
                throw new DataConflictException("Нельзя перенести квартиры в удаляемый дом");
            }

            House newHouse = houseRepository.findById(newHouseId)
                    .orElseThrow(() ->
                            new HouseNotFoundException(
                                    "New house not found id = " + newHouseId
                            )
                    );

            validateFlatTransfer(id, newHouseId);
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

    private void validateFlatTransfer(Long sourceHouseId, Long targetHouseId) {
        List<Flat> flats = new ArrayList<>(flatRepository.findAllByHouse_Id(sourceHouseId));
        if (flats.isEmpty()) {
            return;
        }

        flats.addAll(flatRepository.findAllByHouse_Id(targetHouseId));
        Set<String> names = new HashSet<>();
        Float timeToMetro = flats.get(0).getTimeToMetroOnFoot();

        for (Flat flat : flats) {
            if (!names.add(flat.getName())) {
                throw new DataConflictException(
                        "Перенос создаст квартиры с одинаковым именем в доме id = "
                                + targetHouseId + ": " + flat.getName()
                );
            }
            if (!Objects.equals(timeToMetro, flat.getTimeToMetroOnFoot())) {
                throw new DataConflictException(
                        "Перенос создаст квартиры с разным временем до метро в доме id = " + targetHouseId
                );
            }
        }
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

        Optional<House> houseOpt = houseRepository.findByCoordinates_XAndCoordinates_Y(request.coordinates().x(),
                request.coordinates().y());

        if(houseOpt.isPresent()){
            House houseWithSameCoordinates =  houseOpt.get();
            if (!houseWithSameCoordinates.getId().equals(house.getId())){
                throw new CoordinatesAlreadyExistException("House with same coordinates already exists");
            }
        }

        house.setName(request.name());
        house.setYear(request.year());
        house.setNumberOfFlatsOnFloor(request.numberOfFlatsOnFloor());

        Coordinates coordinates = house.getCoordinates();
        coordinates.setX(request.coordinates().x());
        coordinates.setY(request.coordinates().y());

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
    public List<HouseResponse> importHousesFromFile(User user, MultipartFile file, LocalDateTime startedAt)
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
                                    violation.getMessage())
                    .collect(Collectors.joining("; "));
            throw new ImportValidationException(
                    "Ошибка валидации файла: " + errors
            );
        }

        Optional<CoordinatesDto> validFromFile =
                validateUniqueCoordinatesFromFile(request.houseRequests());

        if (validFromFile.isPresent()) {

            String duplicateCoordinates = validFromFile.get().toString();

            throw new ImportValidationException("Файл содержит дома с повторяющимися координатами = " + duplicateCoordinates);
        }

        List<CoordinatesDto> validFromDB =
                validateUniqueCoordinatesFromDB(request.houseRequests());

        if (!validFromDB.isEmpty()) {

            StringBuilder errorBuilder = new StringBuilder("В базе уже содержаться дома с координатами: \n");
            for (CoordinatesDto coordinatesDto : validFromDB) {
                errorBuilder.append(coordinatesDto).append("\n");
            }

            throw new CoordinatesAlreadyExistException(errorBuilder.toString());
        }

        List<HouseResponse> result = importHouses(request.houseRequests());

        saveImport(user, ImportStatus.SUCCESS, result.size(), startedAt);
        sendWsEventAfterCommit(ChangeEvent.of(ChangeEvent.EntityType.HOUSE, ChangeEvent.ActionType.CREATE, null));
        return result;
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

    private void saveImport(User user, ImportStatus status, int result,LocalDateTime startedAt){
        importHistoryService.saveSuccessfullyImport(
                user,
                ImportType.HOUSE,
                status,
                result,
                startedAt);

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
