package com.example.demo.api;

import com.example.demo.entity.Koi;
import com.example.demo.entity.KoiLot;
import com.example.demo.model.Request.KoiLotRequest;
import com.example.demo.model.Request.KoiRequest;
import com.example.demo.model.Response.KoiLotResponse;
import com.example.demo.model.Response.KoiResponse;
import com.example.demo.service.KoiLotService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/koilot")
@CrossOrigin("*")
@SecurityRequirement(name = "api") //bat buoc phai co
public class KoiLotAPI {
    @Autowired
    KoiLotService koiLotService;

    @PostMapping
    public ResponseEntity create(@Valid @RequestBody KoiLotRequest koiLotRequest){
        KoiLot koiLot = koiLotService.createKoi(koiLotRequest);
        return ResponseEntity.ok(koiLot);
    }

    @PutMapping("{id}")
    public ResponseEntity updateKoi(@Valid @RequestBody KoiLotRequest koiLotRequest, @PathVariable long id) {
        KoiLot newKoi = koiLotService.updateKoi(koiLotRequest,id);
        return ResponseEntity.ok(newKoi);
    }

    @DeleteMapping("{id}")
    public ResponseEntity deleteKoi(@Valid @PathVariable long id) {
        KoiLot newKoi = koiLotService.deleteKoi(id);
        return ResponseEntity.ok(newKoi);
    }

    @GetMapping
    public ResponseEntity getAllKoi(@RequestParam int page, @RequestParam(defaultValue = "5") int size) {
        KoiLotResponse kois = koiLotService.getAllKoi(page, size);
        return ResponseEntity.ok(kois);
    }

    @GetMapping("{name}")
    public ResponseEntity searchKoiByName(String name) {
        KoiLot kois = koiLotService.searchByName(name);
        return ResponseEntity.ok(kois);
    }

    @GetMapping("/by-breed/{breedId}")
    public List<KoiLot> getKoiByBreed(@PathVariable Long breedId) {
        return koiLotService.getKoiLotByBreed(breedId);
    }
}
