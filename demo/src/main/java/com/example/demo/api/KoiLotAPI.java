package com.example.demo.api;

import com.example.demo.entity.KoiLot;
import com.example.demo.model.Request.KoiLotRequest;
import com.example.demo.model.Response.KoiLotPageResponse;
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

//    @GetMapping
//    public ResponseEntity getAllKoi(@RequestParam int page, @RequestParam(defaultValue = "5") int size) {
//        KoiLotResponse kois = koiLotService.getAllKoi(page, size);
//        return ResponseEntity.ok(kois);
//    }
//
//    @GetMapping("{name}")
//    public ResponseEntity searchKoiByName(@PathVariable String name) {
//        KoiLot kois = koiLotService.searchByName(name);
//        return ResponseEntity.ok(kois);
//    }
//
//    @GetMapping("/by-breed/{breedId}")
//    public List<KoiLot> getKoiByBreed(@PathVariable long breedId) {
//        return koiLotService.getKoiLotByBreed(breedId);
//    }

    @GetMapping
    public ResponseEntity<?> getKoi(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "5") Integer size,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "breedId", required = false) Long breedId) {

        // Nếu có tên, tìm kiếm koi theo tên
        if (name != null) {
            KoiLot koi = koiLotService.searchByName(name);
            return ResponseEntity.ok(koi);
        }

        // Nếu có breedId, tìm kiếm koi theo breedId
        if (breedId != null) {
            List<KoiLot> koiByBreed = koiLotService.getKoiLotByBreed(breedId);
            return ResponseEntity.ok(koiByBreed);
        }

        // Nếu có page và size, lấy danh sách koi với phân trang
        if (page != null) {
            KoiLotPageResponse koiPage = koiLotService.getAllKoi(page, size);
            return ResponseEntity.ok(koiPage);
        }

        // Nếu không có tham số nào, trả về lỗi
        return ResponseEntity.badRequest().body("Missing search criteria. Please enter page(0,1,2,...)");
    }

}
