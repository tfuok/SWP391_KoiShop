package com.example.demo.api;

import com.example.demo.entity.Koi;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/koi")
public class KoiAPI {

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
        return ResponseEntity.ok(kois);
    }

    @DeleteMapping("/api/koi/{KoiID}")
    public ResponseEntity<String> deleteKoi(@PathVariable("KoiID") String id) {
        kois.remove(id);
        return ResponseEntity.ok(id);
    }


}
