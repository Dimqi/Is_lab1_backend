package com.example.is_lab1.services;

import com.example.is_lab1.dto.flat.FlatRequest;
import com.example.is_lab1.dto.flat.FlatResponse;
import com.example.is_lab1.entities.*;
import com.example.is_lab1.exceptions.FlatNotFoundException;
import com.example.is_lab1.exceptions.HouseNotFoundException;
import com.example.is_lab1.repositories.FlatRepository;
import com.example.is_lab1.repositories.HouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlatService {

    private final FlatRepository flatRepository;
    private final HouseRepository houseRepository;

    @Transactional(readOnly = true)
    public FlatResponse getFlatById(Long id){
        Flat flat = flatRepository.findById(id)
                .orElseThrow(() -> new FlatNotFoundException("Flat not found id = " + id));

        return FlatResponse.fromEntity(flat);
    }

    @Transactional(readOnly = true)
    public List<FlatResponse> getAllFlats(){
        List<Flat> flats = flatRepository.findAll();
        return flats.stream()
                .map(FlatResponse::fromEntity)
                .toList();
    }


    @Transactional
    public FlatResponse createFlat(FlatRequest flatRequest){
        House houseProxy = null;
        Long houseId = flatRequest.houseId();
        if(houseId != null){
            houseProxy = houseRepository.findById(flatRequest.houseId())
                    .orElseThrow(() -> new HouseNotFoundException("House not found house_id = " + flatRequest.houseId()));
        }


        Flat flat = Flat.builder()
                .name(flatRequest.name())
                .coordinates(Coordinates.builder().x(flatRequest.coordinates().x()).y(flatRequest.coordinates().y()).build())
                .area(flatRequest.area())
                .balcony(flatRequest.balcony())
                .furnish(flatRequest.furnish())
                .timeToMetroOnFoot(flatRequest.timeToMetroOnFoot())
                .transport(flatRequest.transport())
                .price(flatRequest.price())
                .numberOfRooms(flatRequest.numberOfRooms())
                .view(flatRequest.view())
                .creationDate(LocalDate.now())
                .house(houseProxy)
                .build();

        flatRepository.save(flat);


        return FlatResponse.fromEntity(flat);
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

        if (request.coordinates() != null) {
            Coordinates coordinates = Coordinates.builder()
                    .x(request.coordinates().x())
                    .y(request.coordinates().y())
                    .build();
            flat.setCoordinates(coordinates);
        } else {
            flat.setCoordinates(null);
        }


        if (request.houseId() != null) {
            House houseProxy = houseRepository.getReferenceById(request.houseId());
            flat.setHouse(houseProxy);
        }

        return FlatResponse.fromEntity(flat);
    }

    @Transactional
    public void deleteFlat(Long id){
        flatRepository.deleteById(id);
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
    public List<FlatResponse> getAllByTimeToMetro(){
        Sort sort = Sort.by("timeToMetroOnFoot").ascending();
        List<Flat> flats = flatRepository.findAll(sort);
        return flats.stream()
                .map(FlatResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Long sumAllPrices(){
        return flatRepository.sumAllPrices();
    }

}
