package com.example.is_lab1.restApi;

import com.example.is_lab1.dto.house.HouseRequest;
import com.example.is_lab1.dto.house.HouseResponse;
import com.example.is_lab1.services.HouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/house")
@RequiredArgsConstructor
public class HouseController {

    private final HouseService houseService;

    @PostMapping("/create")
    public ResponseEntity<HouseResponse> createHouse(@Valid @RequestBody HouseRequest request){
        HouseResponse responseDto = houseService.createHouse(request);
        return ResponseEntity.status(HttpStatus.CREATED.value())
                .body(responseDto);

    }

    @GetMapping("/all")
    public ResponseEntity<List<HouseResponse>> getAllHouses(){
        List<HouseResponse> response = houseService.getAllHouses();
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<HouseResponse> getById(@PathVariable Long id){
        HouseResponse response = houseService.getHouseById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id){
        houseService.deleteHouseById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }


    @PatchMapping("/{id}")
    public ResponseEntity<HouseResponse> updateById(@Valid @RequestBody HouseRequest request, @PathVariable Long id){
        HouseResponse response = houseService.updateHouse(request, id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }



}
