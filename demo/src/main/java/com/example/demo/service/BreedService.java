package com.example.demo.service;

import com.example.demo.entity.Breed;
import com.example.demo.exception.DuplicatedEntity;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.BreedRequest;
import com.example.demo.repository.BreedRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BreedService {
    @Autowired
    BreedRepository breedRepository;
    @Autowired
    ModelMapper modelMapper;

    public Breed addNewBreed(BreedRequest breedRequest) {
        try {
            Breed breed = modelMapper.map(breedRequest, Breed.class);
            Breed newBreed = breedRepository.findBreedByName(breedRequest.getName());
            if (newBreed != null) throw new DuplicatedEntity("Breed existed!");
            breed.setName(breedRequest.getName());
            return breedRepository.save(breed);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Breed changeBreedName(long id, String name){
        Breed breed = breedRepository.findBreedById(id);
        if (breed == null) throw new NotFoundException("Breed not found");
        breed.setName(name);
        return breedRepository.save(breed);
    }

    public Breed deleteBreed(long id){
        Breed breed = breedRepository.findBreedById(id);
        if (breed == null) throw new NotFoundException("Breed not found");
        breed.setDeleted(true);
        return breedRepository.save(breed);
    }

    public List<Breed> getAllBreeds(){
        List<Breed> breeds = breedRepository.findBreedByIsDeletedFalse();
        return breeds;
    }
}

