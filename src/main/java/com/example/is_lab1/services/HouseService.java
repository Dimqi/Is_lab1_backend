package com.example.is_lab1.services;

import com.example.is_lab1.dto.ChangeEvent;
import com.example.is_lab1.dto.house.HouseFilterRequest;
import com.example.is_lab1.dto.house.HouseRequest;
import com.example.is_lab1.dto.house.HouseResponse;
import com.example.is_lab1.entities.Coordinates;
import com.example.is_lab1.entities.House;
import com.example.is_lab1.exceptions.HouseNotFoundException;
import com.example.is_lab1.repositories.FlatRepository;
import com.example.is_lab1.repositories.HouseRepository;
import com.example.is_lab1.repositories.specifications.HouseSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HouseService {

    private final HouseRepository houseRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final FlatRepository flatRepository;

    @Transactional
    public HouseResponse createHouse(HouseRequest request){
        House house = House.builder()
                .name(request.name())
                .year(request.year())
                .numberOfFlatsOnFloor(request.numberOfFlatsOnFloor())
                .coordinates(
                        Coordinates.builder()
                                .x(request.coordinates().x())
                                .y(request.coordinates().y())
                                .build())
                .build();

        houseRepository.save(house);
        HouseResponse responseDto = HouseResponse.fromEntity(house);

        sendWsEventAfterCommit(
                ChangeEvent.of(ChangeEvent.EntityType.HOUSE, ChangeEvent.ActionType.CREATE, responseDto.id(), responseDto)
        );

        return responseDto;
    }

    @Transactional(readOnly = true)
    public Page<HouseResponse> getAllHouses(HouseFilterRequest filter, Pageable pageable){
        Specification<House> spec = HouseSpecifications.withFilter(filter);

        Page<House> housesPage = houseRepository.findAll(spec, pageable);

        return housesPage.map(HouseResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public HouseResponse getHouseById(Long id){
        House house = houseRepository.findById(id)
                .orElseThrow(() -> new HouseNotFoundException("House not found id =" + id ));

        return HouseResponse.fromEntity(house);
    }

    @Transactional
    public void deleteHouseById(Long id, Long newHouseId) {
        House houseToDelete = houseRepository.findById(id)
                .orElseThrow(() -> new HouseNotFoundException("House not found"));

        if (newHouseId != null) {
            House newHouse = houseRepository.findById(newHouseId)
                    .orElseThrow(() -> new HouseNotFoundException("New house not found id = " + newHouseId));

            flatRepository.updateHouseForFlats(id, newHouse);
        }

        houseRepository.delete(houseToDelete);

        sendWsEventAfterCommit(
                ChangeEvent.of(ChangeEvent.EntityType.HOUSE, ChangeEvent.ActionType.DELETE, id)
        );
    }

    @Transactional
    public HouseResponse updateHouse(HouseRequest request, Long id){
        House house = houseRepository.findById(id)
                .orElseThrow(() -> new HouseNotFoundException("House not found id = " + id));

        house.setName(request.name());
        house.setYear(request.year());
        house.setNumberOfFlatsOnFloor(request.numberOfFlatsOnFloor());

        if (request.coordinates() != null){
            house.setCoordinates(Coordinates.builder()
                    .x(request.coordinates().x())
                    .y(request.coordinates().y())
                    .build());
        }

        HouseResponse responseDto = HouseResponse.fromEntity(house);
        sendWsEventAfterCommit(
                ChangeEvent.of(ChangeEvent.EntityType.HOUSE, ChangeEvent.ActionType.UPDATE, responseDto.id(), responseDto)
        );

        return responseDto;
    }

    private void sendWsEventAfterCommit(ChangeEvent<?> event){
        if(TransactionSynchronizationManager.isActualTransactionActive()){
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    messagingTemplate.convertAndSend("/topic/events", event);
                }
            });
        }
        else {
            messagingTemplate.convertAndSend("/topic/events", event);
        }
    }
}