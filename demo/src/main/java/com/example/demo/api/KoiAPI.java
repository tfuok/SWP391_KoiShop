package com.example.demo.api;

import com.example.demo.entity.Koi;
import com.example.demo.model.Request.KoiRequest;
import com.example.demo.model.Response.KoiPageResponse;
import com.example.demo.model.Response.KoiResponse;
import com.example.demo.service.KoiService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/koi")
@CrossOrigin("*")
@SecurityRequirement(name = "api") //bat buoc phai co
public class KoiAPI {
    @Autowired
    KoiService koiService;

    //method post koi
    @PostMapping
    public ResponseEntity create(@Valid @RequestBody KoiRequest koi) {
        Koi koiRepsponse = koiService.createKoi(koi);
        return ResponseEntity.ok(koiRepsponse);
    }

//    //lay danh sach koi
//    @GetMapping
//    public ResponseEntity getAllKoi() {
//        List<Koi> kois = koiService.getKoi();
//        return ResponseEntity.ok(kois);
//    }

    @DeleteMapping("{id}")
    public ResponseEntity deleteKoi(@Valid @PathVariable long id) {
        Koi newKoi = koiService.deleteKoi(id);
        return ResponseEntity.ok(newKoi);
    }

    @PutMapping("{id}")
    public ResponseEntity updateKoi(@Valid @RequestBody KoiRequest koi, @PathVariable long id) {
        Koi newKoi = koiService.updateKoi(koi, id);
        return ResponseEntity.ok(newKoi);
    }

//    @GetMapping("{name}")
//    public ResponseEntity searchKoiByName(@PathVariable String name) {
//        KoiResponse kois = koiService.searchByName(name);
//        return ResponseEntity.ok(kois);
//    }

    @GetMapping("/compare")
    public ResponseEntity compareKoi(@RequestParam long id1, @RequestParam long id2) {
        Map<String, Object> comparisonResult = koiService.compareKoi(id1, id2);
        return ResponseEntity.ok(comparisonResult);
    }

//    @GetMapping("/by-breed/{breedId}")
//    public List<Koi> getKoiByBreed(@PathVariable Long breedId) {
//        return koiService.getKoiByBreed(breedId);
//    }

    @GetMapping
    public ResponseEntity<?> getKois(
            @RequestParam(required = false) Long breedId,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        if (breedId != null) {
            // If breedId is provided, return Kois filtered by breed
            List<KoiResponse> koisByBreed = koiService.getKoiByBreed(breedId);
            return ResponseEntity.ok(koisByBreed);
        } else if (name != null) {
            // If name is provided, return Kois filtered by name
            List<KoiResponse> koisByName = koiService.searchByName(name);
            return ResponseEntity.ok(koisByName);
        } else {
            // If no filters are provided, return all Kois with pagination
            KoiPageResponse allKois = koiService.getAllKoi(page, size);  // Correctly return KoiPageResponse
            return ResponseEntity.ok(allKois);
        }
    }


}
