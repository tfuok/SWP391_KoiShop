package com.example.demo.api;

import com.example.demo.entity.Koi;
import com.example.demo.model.Request.KoiRequest;
import com.example.demo.model.Request.SaleRequest;
import com.example.demo.model.Response.KoiPageResponse;
import com.example.demo.model.Response.KoiResponse;
import com.example.demo.service.KoiService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/koi")
@CrossOrigin("*")
@SecurityRequirement(name = "api") //bat buoc phai co
public class KoiAPI {
    @Autowired
    KoiService koiLotService;

    @PostMapping
    public ResponseEntity create(@Valid @RequestBody KoiRequest koiLotRequest) {
        Koi koiLot = koiLotService.createKoi(koiLotRequest);
        return ResponseEntity.ok(koiLot);
    }

    @PutMapping("{id}")
    public ResponseEntity updateKoi(@Valid @RequestBody KoiRequest koiLotRequest, @PathVariable long id) {
        Koi newKoi = koiLotService.updateKoi(koiLotRequest, id);
        return ResponseEntity.ok(newKoi);
    }

    @DeleteMapping("{id}")
    public ResponseEntity deleteKoi(@Valid @PathVariable long id) {
        Koi newKoi = koiLotService.deleteKoi(id);
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
//        return koiLotService.getKoiByBreed(breedId);
//    }

    @GetMapping
    public ResponseEntity<?> getKoi(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "5") Integer size,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "breedId", required = false) Long breedId) {

        // Nếu có tên, tìm kiếm koi theo tên
        if (name != null) {
            Koi koi = koiLotService.searchByName(name);
            return ResponseEntity.ok(koi);
        }

        // Nếu có breedId, tìm kiếm koi theo breedId
        if (breedId != null) {
            List<KoiResponse> koiByBreed = koiLotService.getKoiByBreed(breedId);
            return ResponseEntity.ok(koiByBreed);
        }

        // Nếu có page và size, lấy danh sách koi với phân trang
        if (page != null) {
            KoiPageResponse koiPage = koiLotService.getAllKoi(page, size);
            return ResponseEntity.ok(koiPage);
        }

        // Nếu không có tham số nào, trả về lỗi
        return ResponseEntity.badRequest().body("Missing search criteria. Please enter page(0,1,2,...)");
    }

    @GetMapping("/manager")
    public ResponseEntity getKoiForManager(@RequestParam(value = "page", required = false) Integer page,
                                           @RequestParam(value = "size", required = false, defaultValue = "5") Integer size) {
        KoiPageResponse koiPage = koiLotService.getAllKoiManager(page, size);
        return ResponseEntity.ok(koiPage);
    }

    @PostMapping("/sale")
    public ResponseEntity applyDiscount(@RequestBody SaleRequest saleRequest) {
        SaleRequest salePrice = koiLotService.sale(saleRequest);
        return ResponseEntity.ok(salePrice);
    }
}