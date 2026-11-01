package com.example.is_lab1.controllers;

import com.example.is_lab1.dto.flat.FlatFilterRequest;
import com.example.is_lab1.dto.flat.FlatRequest;
import com.example.is_lab1.dto.flat.FlatResponse;
import com.example.is_lab1.entities.Flat;
import com.example.is_lab1.entities.Transport;
import com.example.is_lab1.entities.View;
import com.example.is_lab1.services.FlatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/flat")
@RequiredArgsConstructor
public class FlatController {

    private final FlatService flatService;

    @GetMapping("/all")
    public ResponseEntity<Page<FlatResponse>> getAllFlats(
           Pageable pageable,
           @ModelAttribute FlatFilterRequest filterRequest

    ) {
        return ResponseEntity.ok(flatService.getAllFlats(pageable, filterRequest));
    }


    @PostMapping("/create")
    public ResponseEntity<FlatResponse> createHouse(@Valid @RequestBody FlatRequest request){
        FlatResponse responseDto = flatService.createFlat(request);
        return ResponseEntity.status(HttpStatus.CREATED.value())
                .body(responseDto);

    }

    @GetMapping("/{id}")
    public ResponseEntity<FlatResponse> getById(@PathVariable Long id){
        FlatResponse response = flatService.getFlatById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }


    @PatchMapping("/{id}")
    public ResponseEntity<FlatResponse> updateById(@Valid @RequestBody FlatRequest request, @PathVariable Long id){
        FlatResponse response = flatService.updateFlat(request, id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<FlatResponse> deleteById(@PathVariable Long id){
        flatService.deleteFlat(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/byTransport/{transport}")
    public ResponseEntity<Integer> deleteByTransport(@PathVariable Transport transport){
        Integer result = flatService.deleteByTransport(transport);
        return ResponseEntity.status(HttpStatus.OK)
                .body(result);
    }

    @GetMapping("/count/{view}")
    public ResponseEntity<Integer> countByView(@PathVariable View view){
        Integer result = flatService.countByView(view);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/distinctView")
    public ResponseEntity<List<View>> findDistinctView(){
        List<View> views = flatService.findDistinctView();
        return ResponseEntity.ok(views);
    }

    @GetMapping("/sorted/timeToMetro")
    public ResponseEntity<Page<FlatResponse>> getSortedByTimeToMetro(
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(flatService.getAllByTimeToMetro(pageable));
    }

    @GetMapping("/sumPrices")
    public ResponseEntity<Long> sumAllPrices(){
        Long prices = flatService.sumAllPrices();
        return ResponseEntity.ok(prices);
    }

    @PostMapping(value = "import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importFlats(@RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Передан пустой файл");
        }

        System.out.println("Файл получен: " + file.getOriginalFilename());

        return ResponseEntity.ok()
                .body(flatService.importFlatsFromFile(file));
    }


}
