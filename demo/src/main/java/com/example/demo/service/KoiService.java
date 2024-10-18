package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.KoiRequest;
import com.example.demo.model.Response.KoiPageResponse;
import com.example.demo.model.Response.KoiResponse;
import com.example.demo.repository.BreedRepository;
import com.example.demo.repository.CertificateRepository;
import com.example.demo.repository.KoiRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KoiService {
    @Autowired
    KoiRepository koiLotRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    BreedRepository breedRepository;
    @Autowired
    private CertificateRepository certificateRepository;
    @Autowired
    private CertificateService certificateService;

    public Koi createKoi(KoiRequest koiLotRequest) {
        try {
            // Create a new KoiLot manually without using ModelMapper for the List<Breed>
            Koi koiLot = new Koi();
            koiLot.setName(koiLotRequest.getName());
            koiLot.setPrice(koiLotRequest.getPrice());
            koiLot.setVendor(koiLotRequest.getVendor());
            koiLot.setGender(koiLotRequest.getGender());
            koiLot.setBornYear(koiLotRequest.getBornYear());
            koiLot.setSize(koiLotRequest.getSize());
            koiLot.setOrigin(koiLotRequest.getOrigin());
            koiLot.setDescription(koiLotRequest.getDescription());
            koiLot.setQuantity(koiLotRequest.getQuantity());
            koiLot.setImages(koiLotRequest.getImageUrl());

//            List<Images> imagesList = koiLotRequest.getImagesList().stream().map(imageListRequest -> {
//                Images image = new Images();
//                image.setImages(imageListRequest.getImage());
//                image.setKoi(koiLot);  // Associate the image with the koi
//                return image;
//            }).collect(Collectors.toList());
//
//            koiLot.setImagesList(imagesList);

            // Map breed IDs to Breed entities
            Set<Breed> breeds = new HashSet<>();
            for (Long breedId : koiLotRequest.getBreedId()) {
                Breed breed = breedRepository.findBreedByIdAndIsDeletedFalse(breedId);
                if (breed == null) throw new NotFoundException("Breed not exist");
                breeds.add(breed);
            }
            koiLot.setBreeds(breeds);

            // Set the account of the creator (authenticated user)
            Account accountRequest = authenticationService.getCurrentAccount();
            koiLot.setAccount(accountRequest);
            if (accountRequest.getRole() == Role.CUSTOMER) {
                koiLot.setDeleted(true);
            }



            // Save the KoiLot entity
            koiLotRepository.save(koiLot);
            certificateService.createCertificates(koiLot);
            return koiLot;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create KoiLot");
        }
    }


    public KoiPageResponse getAllKoi(int page, int size) {
        Page<Koi> kois = koiLotRepository.findAllByIsDeletedFalse(PageRequest.of(page, size));
        List<KoiResponse> koiLotResponses = new ArrayList<>();

        for (Koi koi : kois.getContent()) {
            KoiResponse koiLotResponse = modelMapper.map(koi, KoiResponse.class);

            // Only include non-deleted breeds
            List<String> breedNames = koi.getBreeds().stream()
                    .filter(breed -> !breed.isDeleted())
                    .map(Breed::getName)
                    .collect(Collectors.toList());

            koiLotResponse.setBreeds(breedNames);

//            List<String> imageUrls = koi.getImagesList().stream()
//                    .map(Images::getImages)
//                    .collect(Collectors.toList());
//            koiLotResponse.setImagesList(imageUrls);  // Set images list in the response

            koiLotResponses.add(koiLotResponse);
        }

        KoiPageResponse koiResponse = new KoiPageResponse();
        koiResponse.setTotalPages(kois.getTotalPages());
        koiResponse.setContent(koiLotResponses);
        koiResponse.setPageNumber(kois.getNumber());
        koiResponse.setTotalElements(kois.getTotalElements());

        return koiResponse;
    }


    public Koi updateKoi(KoiRequest koiRequest, long id) {
        Koi foundKoi = koiLotRepository.findKoiByIdAndIsDeletedFalse(id);
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

//        List<Images> updatedImagesList = koiRequest.getImagesList().stream().map(imageListRequest -> {
//            Images image = new Images();
//            image.setImages(imageListRequest.getImage());
//            image.setKoi(foundKoi);  // Associate the image with the existing koi
//            return image;
//        }).collect(Collectors.toList());
//        foundKoi.setImagesList(updatedImagesList);

        // Map breed IDs to Breed entities and set the breeds
        Set<Breed> breeds = new HashSet<>();
        for (Long breedId : koiRequest.getBreedId()) {
            Breed breed = breedRepository.findBreedByIdAndIsDeletedFalse(breedId);
            if (breed == null) throw new NotFoundException("Breed not exist");
            breeds.add(breed);
        }
        foundKoi.setBreeds(breeds);

        return koiLotRepository.save(foundKoi);
    }


    public Koi deleteKoi(long id) {
        Koi koi1 = koiLotRepository.findKoiByIdAndIsDeletedFalse(id);
        if (koi1 == null) {
            throw new NotFoundException("Koi not found!");
        }
        koi1.setDeleted(true);
        return koiLotRepository.save(koi1);
    }

    public Koi searchByName(String name) {
        Koi koi = koiLotRepository.findByNameContainingAndIsDeletedFalse(name);
        if (koi == null) throw new NotFoundException("Koi not existed");
//        KoiLotResponse koiLot = modelMapper.map(koi, KoiLotResponse.class);
        return koi;
    }

    public Map<String, Object> compareKoi(long id1, long id2) {
        Koi koi1 = koiLotRepository.findKoiByIdAndIsDeletedFalse(id1);
        Koi koi2 = koiLotRepository.findKoiByIdAndIsDeletedFalse(id2);

        if (koi1 == null || koi2 == null) {
            throw new NotFoundException("One or both Koi not found");
        }

        // So sánh các thuộc tính của 2 cá thể Koi
        Map<String, Object> comparisonResult = new HashMap<>();
        comparisonResult.put("nameMatch", koi1.getName().equals(koi2.getName()));
        comparisonResult.put("priceDifference", Math.abs(koi1.getPrice() - koi2.getPrice()));
        comparisonResult.put("genderMatch", koi1.getGender().equals(koi2.getGender()));
        comparisonResult.put("bornYearDifference", Math.abs(koi1.getBornYear() - koi2.getBornYear()));
        comparisonResult.put("sizeDifference", Math.abs(koi1.getSize() - koi2.getSize()));
        comparisonResult.put("breedMatch", koi1.getBreeds().equals(koi2.getBreeds()));
        comparisonResult.put("originMatch", koi1.getOrigin().equals(koi2.getOrigin()));

        return comparisonResult;
    }

    public List<Koi> getKoiLotByBreed(Long breedId) {
        // Tìm giống cá (Breed) dựa trên breedId
        Breed breed = breedRepository.findBreedByIdAndIsDeletedFalse(breedId);
        if (breed == null) throw new NotFoundException("Breed not found");
        // Tìm tất cả các Koi thuộc giống cá đó
        return koiLotRepository.findByBreedsAndIsDeletedFalse(breed);
    }

    public List<Koi> getKoiLotByCurrentAccount() {
    return koiLotRepository.findByAccountId(authenticationService.getCurrentAccount().getId());
    }
}

