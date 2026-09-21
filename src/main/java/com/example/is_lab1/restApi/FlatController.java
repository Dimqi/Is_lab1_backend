package com.example.is_lab1.restApi;

import com.example.is_lab1.dto.flat.FlatRequest;
import com.example.is_lab1.dto.flat.FlatResponse;
import com.example.is_lab1.dto.house.HouseResponse;
import com.example.is_lab1.entities.Transport;
import com.example.is_lab1.entities.View;
import com.example.is_lab1.services.FlatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/flat")
@RequiredArgsConstructor
public class FlatController {

    private final FlatService flatService;

    @GetMapping("/all")
    public ResponseEntity<List<FlatResponse>> getAllFlats(){
        List<FlatResponse> flatResponses = flatService.getAllFlats();
        return ResponseEntity.ok(flatResponses);
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
    public ResponseEntity<List<FlatResponse>> sortByTimeToMetro(){
        List<FlatResponse> responseList = flatService.getAllByTimeToMetro();
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/sumPrices")
    public ResponseEntity<Long> sumAllPrices(){
        Long prices = flatService.sumAllPrices();
        return ResponseEntity.ok(prices);
    }



}
