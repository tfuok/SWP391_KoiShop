package com.example.demo.api;

import com.example.demo.entity.CareType;
import com.example.demo.exception.DuplicatedEntity;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.CareTypeRequest;
import com.example.demo.service.CareTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/caretypes")
public class CareTypeAPI {

    @Autowired
    private CareTypeService careTypeService;

    @PostMapping
    public ResponseEntity<CareType> addNewCareType(@RequestBody CareTypeRequest careTypeRequest) {
        try {
            CareType careType = careTypeService.addNewCareType(careTypeRequest);
            return ResponseEntity.ok(careType);
        } catch (DuplicatedEntity e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CareType> updateCareType(@RequestBody CareTypeRequest careTypeRequest, @PathVariable long id) {
        try {
            CareType careType = careTypeService.updateCareType(careTypeRequest, id);
            return ResponseEntity.ok(careType);
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCareType(@PathVariable long id) {
        try {
            careTypeService.deleteCareType(id);
            return ResponseEntity.ok().build();
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<CareType>> getAllCareType() {
        try {
            List<CareType> careTypes = careTypeService.getAllCareType();
            return ResponseEntity.ok(careTypes);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}