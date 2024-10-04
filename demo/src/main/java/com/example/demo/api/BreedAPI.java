package com.example.demo.api;

import com.example.demo.entity.Breed;
import com.example.demo.model.Request.BreedRequest;
import com.example.demo.service.BreedService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/breed")
@CrossOrigin("*")
@SecurityRequirement(name = "api") //bat buoc phai co
public class BreedAPI {
    @Autowired
    BreedService breedService;

    @PostMapping
    public ResponseEntity create(BreedRequest breedRequest){
        Breed breed = breedService.addNewBreed(breedRequest);
        return ResponseEntity.ok(breed);
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@PathVariable long id, String name){
        Breed breed = breedService.changeBreedName(id, name);
        return ResponseEntity.ok(breed);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity delete(@PathVariable long id){
        Breed breed = breedService.deleteBreed( id);
        return ResponseEntity.ok(breed);
    }

    @GetMapping
    public ResponseEntity getAll(){
        List<Breed> breeds = breedService.getAllBreeds();
        return ResponseEntity.ok(breeds);
    }
}
