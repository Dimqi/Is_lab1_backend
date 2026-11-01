
package com.example.is_lab1.services;

import com.example.is_lab1.dto.ChangeEvent;
import com.example.is_lab1.dto.flat.FlatFilterRequest;
import com.example.is_lab1.dto.flat.FlatImportRequest;
import com.example.is_lab1.dto.flat.FlatRequest;
import com.example.is_lab1.dto.flat.FlatResponse;
import com.example.is_lab1.entities.*;
import com.example.is_lab1.exceptions.FlatNotFoundException;
import com.example.is_lab1.exceptions.HouseNotFoundException;
import com.example.is_lab1.repositories.FlatRepository;
import com.example.is_lab1.repositories.HouseRepository;
import com.example.is_lab1.repositories.specifications.FlatSpecifications;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
public class FlatService {

    private final FlatRepository flatRepository;
    private final HouseRepository houseRepository;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final Validator validator;

    @Transactional(readOnly = true)
    public FlatResponse getFlatById(Long id) {
        Flat flat = flatRepository.findById(id)
                .orElseThrow(() -> new FlatNotFoundException("Flat not found id = " + id));
        return FlatResponse.fromEntity(flat);
    }

    @Transactional(readOnly = true)
    public Page<FlatResponse> getAllFlats(Pageable pageable, FlatFilterRequest filter) {
        Specification<Flat> spec = FlatSpecifications.build(filter);
        Page<Flat> flatsPage = flatRepository.findAll(spec, pageable);
        return flatsPage.map(FlatResponse::fromEntity);
    }

    @Retryable(
            retryFor = Exception.class,
            exceptionExpression = "@serializationFailure.isSerializationFailure(#root)",
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public FlatResponse createFlat(FlatRequest flatRequest) {
        House house = null;
        Long houseId = flatRequest.houseId();

        if (houseId != null) {
            house = houseRepository.findById(houseId)
                    .orElseThrow(() -> new HouseNotFoundException("House not found house_id = " + houseId));
        }

        if (flatRepository.existsByNameAndHouse(flatRequest.name(), house)) {
            throw new RuntimeException("Flat with same name already exists in this house");
        }

        if (houseId != null) {
            Optional<Flat> repFlat = flatRepository.findFirstByHouse_Id(houseId);

            if (repFlat.isPresent()) {
                Float timeToMetro = repFlat.get().getTimeToMetroOnFoot();

                if (!Objects.equals(timeToMetro, flatRequest.timeToMetroOnFoot())) {
                    throw new RuntimeException(
                            "Квартиры в одном доме не могут иметь различное время до метро. Рекомендуемое значение = " + timeToMetro
                    );
                }
            }
        }

        Flat flat = buildFlat(flatRequest, house);
        flatRepository.save(flat);
        FlatResponse responseDto = FlatResponse.fromEntity(flat);

        sendWsEventAfterCommit(
                ChangeEvent.of(
                        ChangeEvent.EntityType.FLAT,
                        ChangeEvent.ActionType.CREATE,
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
    public FlatResponse updateFlat(FlatRequest request, Long id) {
        Flat flat = flatRepository.findById(id)
                .orElseThrow(() -> new FlatNotFoundException("Flat not found id = " + id));

        flat.setName(request.name());
        flat.setArea(request.area());
        flat.setPrice(request.price());
        flat.setBalcony(request.balcony());
        flat.setTimeToMetroOnFoot(request.timeToMetroOnFoot());
        flat.setNumberOfRooms(request.numberOfRooms());
        flat.setFurnish(request.furnish());
        flat.setView(request.view());
        flat.setTransport(request.transport());

        Long houseId = request.houseId();
        House house = null;

        if (houseId != null) {
            house = houseRepository.findById(houseId)
                    .orElseThrow(() -> new HouseNotFoundException("House not found house_id = " + houseId));
        }

        flat.setHouse(house);

        if (flatRepository.existsByNameAndHouseAndIdNot(request.name(), house, id)) {
            throw new RuntimeException("Flat with same name already exists in this house");
        }

        if (houseId != null) {
            Optional<Flat> repFlat = flatRepository.findFirstByHouse_IdAndIdNot(houseId, id);

            if (repFlat.isPresent()) {
                Float timeToMetro = repFlat.get().getTimeToMetroOnFoot();

                if (!Objects.equals(timeToMetro, request.timeToMetroOnFoot())) {
                    throw new RuntimeException(
                            "Квартиры в одном доме не могут иметь различное время до метро. Рекомендуемое значение = " + timeToMetro
                    );
                }
            }
        }

        FlatResponse responseDto = FlatResponse.fromEntity(flat);

        sendWsEventAfterCommit(
                ChangeEvent.of(
                        ChangeEvent.EntityType.FLAT,
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
    public void deleteFlat(Long id) {
        flatRepository.deleteById(id);

        sendWsEventAfterCommit(
                ChangeEvent.of(
                        ChangeEvent.EntityType.FLAT,
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
    public Integer deleteByTransport(Transport transport) {
        return flatRepository.deleteByTransport(transport);
    }

    @Transactional(readOnly = true)
    public Integer countByView(View view) {
        return flatRepository.countByView(view);
    }

    @Transactional(readOnly = true)
    public List<View> findDistinctView() {
        return flatRepository.findDistinctView();
    }

    @Transactional(readOnly = true)
    public Page<FlatResponse> getAllByTimeToMetro(Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("timeToMetroOnFoot").ascending()
        );

        Page<Flat> flatsPage = flatRepository.findAll(sortedPageable);
        return flatsPage.map(FlatResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Long sumAllPrices() {
        return flatRepository.sumAllPrices();
    }

    @Retryable(
            retryFor = Exception.class,
            exceptionExpression = "@serializationFailure.isSerializationFailure(#root)",
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public List<FlatResponse> importFlatsFromFile(MultipartFile file) throws IOException {
        FlatImportRequest request = objectMapper.readValue(file.getInputStream(), FlatImportRequest.class);
        List<FlatRequest> flatRequests = request.flatRequests();

        validateHousesExist(flatRequests);

        Set<ConstraintViolation<FlatImportRequest>> violations = validator.validate(request);

        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .collect(Collectors.joining("; "));

            throw new IllegalArgumentException("Ошибка валидации файла: " + errors);
        }

        Optional<FlatRequest> duplicateName = validateUniqueFlatNamesFromFile(flatRequests);

        if (duplicateName.isPresent()) {
            FlatRequest flat = duplicateName.get();

            throw new IllegalArgumentException(
                    "Файл содержит квартиры с повторяющимся названием: " + flat.name() + ", houseId = " + flat.houseId()
            );
        }

        List<FlatResponse> duplicateNamesFromDB = validateUniqueFlatNamesFromDB(flatRequests);

        if (!duplicateNamesFromDB.isEmpty()) {
            throw new IllegalArgumentException(
                    "В базе уже существуют квартиры с такими названиями в соответствующих домах: " + duplicateNamesFromDB
            );
        }

        Optional<FlatRequest> differentTime = validateTimeToMetroFromFile(flatRequests);

        if (differentTime.isPresent()) {
            FlatRequest flat = differentTime.get();

            throw new IllegalArgumentException(
                    "Файл содержит разные значения timeToMetroOnFoot для дома с id = " + flat.houseId()
            );
        }

        List<FlatResponse> differentTimeFromDB = validateTimeToMetroFromDB(flatRequests);

        if (!differentTimeFromDB.isEmpty()) {
            throw new IllegalArgumentException(
                    "Время до метро в файле отличается от времени у существующих квартир в соответствующих домах: " + differentTimeFromDB
            );
        }

        return importFlats(flatRequests);
    }

    private List<FlatResponse> importFlats(List<FlatRequest> flatRequests) {
        List<Flat> flats = flatRequests.stream()
                .map(flatRequest -> {
                    House house = houseRepository.getReferenceById(flatRequest.houseId());
                    return buildFlat(flatRequest, house);
                })
                .toList();

        return flatRepository.saveAllAndFlush(flats)
                .stream()
                .map(FlatResponse::fromEntity)
                .toList();
    }

    private Optional<FlatRequest> validateUniqueFlatNamesFromFile(List<FlatRequest> flatRequests) {
        Set<String> uniqueNames = new HashSet<>();

        for (FlatRequest flatRequest : flatRequests) {
            String key = flatRequest.houseId() + ":" + flatRequest.name();

            if (!uniqueNames.add(key)) {
                return Optional.of(flatRequest);
            }
        }

        return Optional.empty();
    }

    private List<FlatResponse> validateUniqueFlatNamesFromDB(List<FlatRequest> flatRequests) {
        Specification<Flat> spec = FlatSpecifications.withNamesUsedByHouse(flatRequests);

        return flatRepository.findAll(spec)
                .stream()
                .map(FlatResponse::fromEntity)
                .toList();
    }

    private Optional<FlatRequest> validateTimeToMetroFromFile(List<FlatRequest> flatRequests) {
        Map<Long, Float> timeByHouse = new HashMap<>();

        for (FlatRequest flatRequest : flatRequests) {
            Long houseId = flatRequest.houseId();
            Float timeToMetro = flatRequest.timeToMetroOnFoot();

            Float existingTime = timeByHouse.putIfAbsent(houseId, timeToMetro);

            if (!Objects.equals(existingTime, timeToMetro)) {
                return Optional.of(flatRequest);
            }
        }

        return Optional.empty();
    }

    private List<FlatResponse> validateTimeToMetroFromDB(List<FlatRequest> flatRequests) {
        Specification<Flat> spec = FlatSpecifications.withDifferentTimeToMetro(flatRequests);

        return flatRepository.findAll(spec)
                .stream()
                .map(FlatResponse::fromEntity)
                .toList();
    }

    private Flat buildFlat(FlatRequest flatRequest, House house) {
        return Flat.builder()
                .name(flatRequest.name())
                .area(flatRequest.area())
                .price(flatRequest.price())
                .balcony(flatRequest.balcony())
                .timeToMetroOnFoot(flatRequest.timeToMetroOnFoot())
                .numberOfRooms(flatRequest.numberOfRooms())
                .furnish(flatRequest.furnish())
                .view(flatRequest.view())
                .transport(flatRequest.transport())
                .house(house)
                .build();
    }

    private void validateHousesExist(List<FlatRequest> flatRequests) {
        Set<Long> requestedHouseIds = flatRequests.stream()
                .map(FlatRequest::houseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> existingHouseIds = new HashSet<>(houseRepository.findExistingIds(requestedHouseIds));

        requestedHouseIds.removeAll(existingHouseIds);

        if (!requestedHouseIds.isEmpty()) {
            throw new HouseNotFoundException("Houses not found: " + requestedHouseIds);
        }
    }

    private void sendWsEventAfterCommit(ChangeEvent<?> event) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    messagingTemplate.convertAndSend("/topic/events", event);
                }
            });
        } else {
            messagingTemplate.convertAndSend("/topic/events", event);
        }
    }
}
