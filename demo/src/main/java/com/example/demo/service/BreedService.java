package com.example.demo.service;

import com.example.demo.entity.Breed;
import com.example.demo.entity.Koi;
import com.example.demo.exception.DuplicatedEntity;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.BreedRequest;
import com.example.demo.repository.BreedRepository;
import com.example.demo.repository.KoiRepository;
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
    @Autowired
    KoiRepository koiRepository;
    public Breed addNewBreed(BreedRequest breedRequest) {
        try {
            Breed breed = modelMapper.map(breedRequest, Breed.class);
            Breed newBreed = breedRepository.findBreedByNameAndIsDeletedFalse(breedRequest.getName());
            if (newBreed != null) throw new DuplicatedEntity("Breed existed!");
            breed.setName(breedRequest.getName());
            breed.setImageUrl(breedRequest.getImageUrl());
            return breedRepository.save(breed);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Breed changeBreedName(long id, BreedRequest breedRequest){
        // Find the breed by its ID
        Breed existingBreed = breedRepository.findBreedByIdAndIsDeletedFalse(id);

        // If the breed is not found, throw an exception
        if (existingBreed == null) throw new NotFoundException("Breed not found");

        // Update the existing breed's name with the new name from the request
        existingBreed.setName(breedRequest.getName());
        existingBreed.setImageUrl(breedRequest.getImageUrl());
        // Save the updated breed back into the repository
        return breedRepository.save(existingBreed);
    }

    public Breed deleteBreed(long id){
        Breed breed = breedRepository.findBreedByIdAndIsDeletedFalse(id);
        if (breed == null) throw new NotFoundException("Breed not found");
        breed.setDeleted(true);
        List<Koi> associatedKois = koiRepository.findByBreedsAndIsDeletedFalse(breed);
        for (Koi koi : associatedKois) {
            koi.setDeleted(true);
            koiRepository.save(koi);
        }
        return breedRepository.save(breed);
    }

    public List<Breed> getAllBreeds(){
        List<Breed> breeds = breedRepository.findBreedByIsDeletedFalse();
        return breeds;
    }
}

