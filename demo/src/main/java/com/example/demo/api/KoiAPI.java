package com.example.demo.api;

import com.example.demo.entity.Koi;
import com.example.demo.service.KoiService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/koi")
@CrossOrigin("*")
public class KoiAPI {
    @Autowired
    KoiService koiService;

    List<Koi> kois = new ArrayList<>();

    //method post koi
    @PostMapping
    public ResponseEntity create(@Valid @RequestBody Koi koi) {
        kois.add(koi);
        return ResponseEntity.ok(koi);
    }

    //lay danh sach koi
    @GetMapping
    public ResponseEntity getAllKoi() {
        List<Koi> kois = koiService.getAllKoi();
        return ResponseEntity.ok(kois);
    }

    @DeleteMapping("{id}")
    public ResponseEntity deleteKoi(@Valid @RequestBody Koi koi, @PathVariable long id) {
        Koi newKoi = koiService.deleteKoi(koi, id);
        return ResponseEntity.ok(newKoi);
    }

    @PutMapping("{id}")
    public ResponseEntity updateKoi(@Valid @RequestBody Koi koi, @PathVariable long id) {
        Koi newKoi = koiService.updateKoi(koi, id);
        return ResponseEntity.ok(newKoi);
    }
}
