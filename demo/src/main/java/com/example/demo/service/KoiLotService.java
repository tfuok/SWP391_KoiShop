package com.example.demo.service;

import com.example.demo.entity.Account;
import com.example.demo.entity.Breed;
import com.example.demo.entity.KoiLot;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.KoiLotRequest;
import com.example.demo.model.Response.KoiLotPageResponse;
import com.example.demo.model.Response.KoiLotResponse;
import com.example.demo.repository.BreedRepository;
import com.example.demo.repository.KoiLotRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KoiLotService {
    @Autowired
    KoiLotRepository koiLotRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    BreedRepository breedRepository;

    public KoiLot createKoi(KoiLotRequest koiLotRequest) {
        try {
            // Create a new KoiLot manually without using ModelMapper for the List<Breed>
            KoiLot koiLot = new KoiLot();
            koiLot.setName(koiLotRequest.getName());
            koiLot.setPrice(koiLotRequest.getPrice());
            koiLot.setVendor(koiLotRequest.getVendor());
            koiLot.setGender(koiLotRequest.getGender());
            koiLot.setBornYear(koiLotRequest.getBornYear());
            koiLot.setSize(koiLotRequest.getSize());
            koiLot.setOrigin(koiLotRequest.getOrigin());
            koiLot.setDescription(koiLotRequest.getDescription());
            koiLot.setQuantity(koiLotRequest.getQuantity());
            koiLot.setImages(koiLot.getImages());

            // Map breed IDs to Breed entities
            List<Breed> breeds = new ArrayList<>();
            for (Long breedId : koiLotRequest.getBreedId()) {
                Breed breed = breedRepository.findBreedByIdAndIsDeletedFalse(breedId);
                if (breed == null) throw new NotFoundException("Breed not exist");
                breeds.add(breed);
            }
            koiLot.setBreeds(breeds);

            // Set the account of the creator (authenticated user)
            Account accountRequest = authenticationService.getCurrentAccount();
            koiLot.setAccount(accountRequest);

            // Save the KoiLot entity
            return koiLotRepository.save(koiLot);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create KoiLot");
        }
    }


    public KoiLotPageResponse getAllKoi(int page, int size) {
        Page<KoiLot> kois = koiLotRepository.findAllByIsDeletedFalse(PageRequest.of(page, size));
        // Convert List<KoiLot> to List<KoiLotResponse>
        List<KoiLotResponse> koiLotResponses = new ArrayList<>();
        for (KoiLot koi : kois.getContent()) {
            KoiLotResponse koiLotResponse = modelMapper.map(koi, KoiLotResponse.class);

            List<String> breedNames = koi.getBreeds().stream()
                    .map(Breed::getName) // Map each Breed entity to its name
                    .collect(Collectors.toList());
            koiLotResponse.setBreeds(breedNames);
            koiLotResponses.add(koiLotResponse);
        }

        // Set content and other details in KoiLotPageResponse
        KoiLotPageResponse koiResponse = new KoiLotPageResponse();
        koiResponse.setTotalPages(kois.getTotalPages());
        koiResponse.setContent(koiLotResponses); // Set List<KoiLotResponse>
        koiResponse.setPageNumber(kois.getNumber());
        koiResponse.setTotalElements(kois.getTotalElements());

        return koiResponse;
    }

    public KoiLot updateKoi(KoiLotRequest koiRequest, long id) {
        KoiLot foundKoi = koiLotRepository.findKoiLotById(id);
        if (foundKoi == null) {
            throw new NotFoundException("Koi not found");
        }

        // Update other fields manually
        foundKoi.setName(koiRequest.getName());
        foundKoi.setPrice(koiRequest.getPrice());
        foundKoi.setVendor(koiRequest.getVendor());
        foundKoi.setGender(koiRequest.getGender());
        foundKoi.setBornYear(koiRequest.getBornYear());
        foundKoi.setSize(koiRequest.getSize());
        foundKoi.setOrigin(koiRequest.getOrigin());
        foundKoi.setDescription(koiRequest.getDescription());
        foundKoi.setQuantity(koiRequest.getQuantity());
        foundKoi.setImages(koiRequest.getImageUrl());
        // Map breed IDs to Breed entities and set the breeds
        List<Breed> breeds = new ArrayList<>();
        for (Long breedId : koiRequest.getBreedId()) {
            Breed breed = breedRepository.findBreedByIdAndIsDeletedFalse(breedId);
            if (breed == null) throw new NotFoundException("Breed not exist");
            breeds.add(breed);
        }
        foundKoi.setBreeds(breeds);

        return koiLotRepository.save(foundKoi);
    }


    public KoiLot deleteKoi(long id) {
        KoiLot koi1 = koiLotRepository.findKoiLotById(id);
        if (koi1 == null) {
            throw new NotFoundException("Koi not found!");
        }
        koi1.setDeleted(true);
        return koiLotRepository.save(koi1);
    }

    public KoiLot searchByName(String name) {
        KoiLot koi = koiLotRepository.findByNameContainingAndIsDeletedFalse(name);
        if (koi == null) throw new NotFoundException("Koi not existed");
//        KoiLotResponse koiLot = modelMapper.map(koi, KoiLotResponse.class);
        return koi;
    }

//    public Map<String, Object> compareKoi(long id1, long id2) {
//        Koi koi1 = koiLotRepository.findKoiByIdAndIsDeletedFalse(id1);
//        Koi koi2 = koiLotRepository.findKoiByIdAndIsDeletedFalse(id2);
//
//        if (koi1 == null || koi2 == null) {
//            throw new NotFoundException("One or both Koi not found");
//        }
//
//        // So sánh các thuộc tính của 2 cá thể Koi
//        Map<String, Object> comparisonResult = new HashMap<>();
//        comparisonResult.put("nameMatch", koi1.getName().equals(koi2.getName()));
//        comparisonResult.put("priceDifference", Math.abs(koi1.getPrice() - koi2.getPrice()));
//        comparisonResult.put("genderMatch", koi1.getGender().equals(koi2.getGender()));
//        comparisonResult.put("bornYearDifference", Math.abs(koi1.getBornYear() - koi2.getBornYear()));
//        comparisonResult.put("sizeDifference", Math.abs(koi1.getSize() - koi2.getSize()));
//        comparisonResult.put("breedMatch", koi1.getBreed().equals(koi2.getBreed()));
//        comparisonResult.put("originMatch", koi1.getOrigin().equals(koi2.getOrigin()));
//
//        return comparisonResult;
//    }

    public List<KoiLot> getKoiLotByBreed(Long breedId) {
        // Tìm giống cá (Breed) dựa trên breedId
        Breed breed = breedRepository.findBreedByIdAndIsDeletedFalse(breedId);
        if (breed == null) throw new NotFoundException("Breed not found");
        // Tìm tất cả các Koi thuộc giống cá đó
        return koiLotRepository.findByBreeds(breed);
    }

}
