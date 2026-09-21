package com.example.is_lab1.services;

import com.example.is_lab1.dto.ChangeEvent;
import com.example.is_lab1.dto.house.HouseRequest;
import com.example.is_lab1.dto.house.HouseResponse;
import com.example.is_lab1.entities.House;
import com.example.is_lab1.exceptions.HouseNotFoundException;
import com.example.is_lab1.repositories.HouseRepository;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public HouseResponse createHouse(HouseRequest request){
        House house = House.builder()
                .name(request.name())
                .year(request.year())
                .numberOfFlatsOnFloor(request.numberOfFlatsOnFloor())
                .build();

        houseRepository.save(house);
        HouseResponse responseDto = HouseResponse.fromEntity(house);

        sendWsEventAfterCommit(
                ChangeEvent.of(ChangeEvent.EntityType.HOUSE, ChangeEvent.ActionType.UPDATE, responseDto.id(), responseDto)
        );

        return responseDto;
    }

    @Transactional(readOnly = true)
    public List<HouseResponse> getAllHouses(){
        List<House> houses = houseRepository.findAll();
        return houses.stream()
                .map(HouseResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public HouseResponse getHouseById(Long id){
        House house = houseRepository.findById(id)
                .orElseThrow(() -> new HouseNotFoundException("House not found id =" + id ));

        return HouseResponse.fromEntity(house);
    }


    @Transactional
    public void deleteHouseById(Long id){
        houseRepository.deleteById(id);

        sendWsEventAfterCommit(
                ChangeEvent.of(ChangeEvent.EntityType.HOUSE, ChangeEvent.ActionType.UPDATE, id)
        );


    }

    @Transactional
    public HouseResponse updateHouse(HouseRequest request, Long id){
        House house = houseRepository.findById(id)
                .orElseThrow(() -> new HouseNotFoundException("House not found id = " + id));

        house.setName(request.name());
        house.setYear(request.year());
        house.setNumberOfFlatsOnFloor(request.numberOfFlatsOnFloor());

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
                    messagingTemplate.convertAndSend("/topic/events",event);
                }
            });
        }
        else {
            messagingTemplate.convertAndSend("/topic/events",event);
        }
    }


}
