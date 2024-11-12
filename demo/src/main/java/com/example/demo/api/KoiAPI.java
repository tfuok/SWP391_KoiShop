package com.example.demo.api;

import com.example.demo.entity.Koi;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.KoiRequest;
import com.example.demo.model.Request.SaleRequest;
import com.example.demo.model.Response.KoiPageResponse;
import com.example.demo.model.Response.KoiResponse;
import com.example.demo.service.KoiService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity sale(@RequestBody SaleRequest saleRequest) {
        SaleRequest salePrice = koiLotService.sale(saleRequest);
        return ResponseEntity.ok(salePrice);
    }

    @PostMapping("/unSale")
    public ResponseEntity unSale(long id) {
        koiLotService.unSale(id);
        return ResponseEntity.ok("Success");
    }

    @GetMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareKoi(
            @RequestParam("id1") long id1,
            @RequestParam("id2") long id2,
            @RequestParam("comparisonType") String comparisonType) {
        try {
            Map<String, Object> comparisonResult;
            switch (comparisonType.toLowerCase()) {
                case "unique":
                    comparisonResult = koiLotService.compareUniqueKoi(id1, id2);
                    break;
                case "lot":
                    comparisonResult = koiLotService.compareKoiLots(id1, id2);
                    break;
                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "Invalid comparison type"));
            }
            return ResponseEntity.ok(comparisonResult);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/undelete")
    public ResponseEntity unDeleteKoi(long id) {
        koiLotService.unDelete(id);
        return ResponseEntity.ok("success");
    }
}