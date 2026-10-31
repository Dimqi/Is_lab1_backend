package com.example.is_lab1.controllers;

import com.example.is_lab1.dto.flat.FlatRequest;
import com.example.is_lab1.dto.house.HouseFilterRequest;
import com.example.is_lab1.dto.house.HouseRequest;
import com.example.is_lab1.dto.house.HouseResponse;

import com.example.is_lab1.services.HouseService;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/house")
@RequiredArgsConstructor
public class HouseController {

    private final HouseService houseService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @PostMapping("/create")
    public ResponseEntity<HouseResponse> createHouse(@Valid @RequestBody HouseRequest request){
        HouseResponse responseDto = houseService.createHouse(request);
        return ResponseEntity.status(HttpStatus.CREATED.value())
                .body(responseDto);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<HouseResponse>> getAllHouses(
            @ModelAttribute HouseFilterRequest filter,
            Pageable pageable
    ) {
        Page<HouseResponse> response = houseService.getAllHouses(filter, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HouseResponse> getById(@PathVariable Long id){
        HouseResponse response = houseService.getHouseById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id,
    @RequestParam(required = false) Long newHouseId){
        houseService.deleteHouseById(id, newHouseId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<HouseResponse> updateById(@Valid @RequestBody HouseRequest request, @PathVariable Long id){
        HouseResponse response = houseService.updateHouse(request, id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping(value = "import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importHoses(@RequestParam("file") MultipartFile file) throws IOException {
        if(file.isEmpty()){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Передан пустой файл");
        }


        System.out.println("Файл получен: " + file.getOriginalFilename());

        return ResponseEntity.ok().body(houseService.importHousesFromFile(file));
    }

}