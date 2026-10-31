package com.example.is_lab1.services;

import com.example.is_lab1.dto.ChangeEvent;
import com.example.is_lab1.dto.flat.FlatFilterRequest;
import com.example.is_lab1.dto.flat.FlatRequest;
import com.example.is_lab1.dto.flat.FlatResponse;
import com.example.is_lab1.entities.*;
import com.example.is_lab1.exceptions.FlatNotFoundException;
import com.example.is_lab1.exceptions.HouseNotFoundException;
import com.example.is_lab1.repositories.FlatRepository;
import com.example.is_lab1.repositories.HouseRepository;
import com.example.is_lab1.repositories.specifications.FlatSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FlatService {

    private final FlatRepository flatRepository;
    private final HouseRepository houseRepository;

    private final SimpMessagingTemplate messagingTemplate;


    @Transactional(readOnly = true)
    public FlatResponse getFlatById(Long id){
        Flat flat = flatRepository.findById(id)
                .orElseThrow(() -> new FlatNotFoundException("Flat not found id = " + id));

        return FlatResponse.fromEntity(flat);
    }

    @Transactional(readOnly = true)
    public Page<FlatResponse> getAllFlats(Pageable pageable,
                                          FlatFilterRequest filter) {
        Specification<Flat> spec = FlatSpecifications.build(filter);

        Page<Flat> flatsPage = flatRepository.findAll(spec, pageable);
        return flatsPage.map(FlatResponse::fromEntity);
    }


    @Transactional
    public FlatResponse createFlat(FlatRequest flatRequest) {
        House house = null;
        Long houseId = flatRequest.houseId();

        if (houseId != null) {
            house = houseRepository.findByIdForUpdate(houseId)
                    .orElseThrow(() -> new HouseNotFoundException(
                            "House not found house_id = " + houseId
                    ));
        }

        if (flatRepository.existsByNameAndHouse(flatRequest.name(), house)) {
            throw new RuntimeException("Flat with same name already exists in this house");
        }

        if (houseId != null) {
            Optional<Flat> repFlat =
                    flatRepository.findFirstByHouse_Id(houseId);

            if (repFlat.isPresent()) {
                Float timeToMetro =
                        repFlat.get().getTimeToMetroOnFoot();

                if (!timeToMetro.equals(flatRequest.timeToMetroOnFoot())) {
                    throw new RuntimeException(
                            "Квартиры в одном доме не могут иметь " +
                                    "различное время до метро. Рекомендуемое " +
                                    "значение = " + timeToMetro
                    );
                }
            }
        }

        Flat flat = Flat.builder()
                .name(flatRequest.name())
                .area(flatRequest.area())
                .balcony(flatRequest.balcony())
                .furnish(flatRequest.furnish())
                .timeToMetroOnFoot(flatRequest.timeToMetroOnFoot())
                .transport(flatRequest.transport())
                .price(flatRequest.price())
                .numberOfRooms(flatRequest.numberOfRooms())
                .view(flatRequest.view())
                .creationDate(LocalDate.now())
                .house(house)
                .build();

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




    @Transactional
    public FlatResponse updateFlat(FlatRequest request, Long id){
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


        if (request.houseId() != null) {
            House houseProxy = houseRepository.getReferenceById(request.houseId());
            flat.setHouse(houseProxy);
        }

        FlatResponse responseDto = FlatResponse.fromEntity(flat);

        sendWsEventAfterCommit(
                ChangeEvent.of(ChangeEvent.EntityType.FLAT, ChangeEvent.ActionType.UPDATE, responseDto.id(), responseDto)
        );

        return responseDto;
    }

    @Transactional
    public void deleteFlat(Long id){
        flatRepository.deleteById(id);
        sendWsEventAfterCommit(
                ChangeEvent.of(ChangeEvent.EntityType.FLAT, ChangeEvent.ActionType.DELETE, id)
        );
    }

    @Transactional(readOnly = true)
    public Integer deleteByTransport(Transport transport){
        return flatRepository.deleteByTransport(transport);
    }

    @Transactional(readOnly = true)
    public Integer countByView(View view){
        return flatRepository.countByView(view);
    }


    @Transactional(readOnly = true)
    public List<View> findDistinctView(){
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
    public Long sumAllPrices(){
        return flatRepository.sumAllPrices();
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
