package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.KoiRequest;
import com.example.demo.model.Request.SaleRequest;
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

import java.time.LocalDateTime;
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

            List<Images> imagesList = koiLotRequest.getImagesList().stream().map(imageListRequest -> {
                Images image = new Images();
                image.setImages(imageListRequest.getImage());
                image.setKoi(koiLot);  // Associate the image with the koi
                return image;
            }).collect(Collectors.toList());

            koiLot.setImagesList(imagesList);

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
                koiLot.setConsignment(true);
            }
            // Save the KoiLot entity
            koiLotRepository.save(koiLot);
            if (koiLot.getQuantity() == 1) {
                certificateService.createCertificates(koiLot);
            }
            return koiLot;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create KoiLot");
        }
    }


    public KoiPageResponse getAllKoi(int page, int size) {
        try {
            Page<Koi> kois = koiLotRepository.findAllByIsDeletedFalseAndSoldFalse(PageRequest.of(page, size));
            List<KoiResponse> koiLotResponses = new ArrayList<>();

            for (Koi koi : kois.getContent()) {
                KoiResponse koiLotResponse = modelMapper.map(koi, KoiResponse.class);

                // Only include non-deleted breeds
                List<String> breedNames = koi.getBreeds().stream()
                        .filter(breed -> !breed.isDeleted())
                        .map(Breed::getName)
                        .collect(Collectors.toList());
                koiLotResponse.setBreeds(breedNames);

                // Map image URLs
                List<String> imageUrls = koi.getImagesList().stream()
                        .map(Images::getImages)
                        .collect(Collectors.toList());
                koiLotResponse.setImagesList(imageUrls);  // Set images list in the response

                // Check if the certificate exists before accessing its properties
                if (koiLotResponse.getQuantity() == 1 && koi.getCertificate() != null) {
                    String certificateImageUrl = koi.getCertificate().getImageUrl();
                    koiLotResponse.setCertificate(certificateImageUrl);
                } else {
                    koiLotResponse.setCertificate(null); // Handle case where no certificate exists
                }

                koiLotResponses.add(koiLotResponse);
            }

            // Prepare the response
            KoiPageResponse koiResponse = new KoiPageResponse();
            koiResponse.setTotalPages(kois.getTotalPages());
            koiResponse.setContent(koiLotResponses);
            koiResponse.setPageNumber(kois.getNumber());
            koiResponse.setTotalElements(kois.getTotalElements());

            return koiResponse;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch koi data", e); // Provide meaningful error messages
        }
    }

    public KoiPageResponse getAllKoiManager(int page, int size) {
        try {
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

                // Map image URLs
                List<String> imageUrls = koi.getImagesList().stream()
                        .map(Images::getImages)
                        .collect(Collectors.toList());
                koiLotResponse.setImagesList(imageUrls);  // Set images list in the response

                // Check if the certificate exists before accessing its properties
                if (koiLotResponse.getQuantity() == 1 && koi.getCertificate() != null) {
                    String certificateImageUrl = koi.getCertificate().getImageUrl();
                    koiLotResponse.setCertificate(certificateImageUrl);
                } else {
                    koiLotResponse.setCertificate(null); // Handle case where no certificate exists
                }

                koiLotResponses.add(koiLotResponse);
            }

            // Prepare the response
            KoiPageResponse koiResponse = new KoiPageResponse();
            koiResponse.setTotalPages(kois.getTotalPages());
            koiResponse.setContent(koiLotResponses);
            koiResponse.setPageNumber(kois.getNumber());
            koiResponse.setTotalElements(kois.getTotalElements());
            return koiResponse;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch koi data", e); // Provide meaningful error messages
        }
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

        List<Images> updatedImagesList = koiRequest.getImagesList().stream().map(imageListRequest -> {
            Images image = new Images();
            image.setImages(imageListRequest.getImage());
            image.setKoi(foundKoi);
            return image;
        }).collect(Collectors.toList());
        foundKoi.setImagesList(updatedImagesList);

        // Map breed IDs to Breed entities and set the breeds
        Set<Breed> breeds = new HashSet<>();
        for (Long breedId : koiRequest.getBreedId()) {
            Breed breed = breedRepository.findBreedByIdAndIsDeletedFalse(breedId);
            if (breed == null) throw new NotFoundException("Breed not exist");
            breed.getKois().add(foundKoi);
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
        Koi koi = koiLotRepository.findByNameContainingAndIsDeletedFalseAndSoldFalse(name);
        if (koi == null) throw new NotFoundException("Koi not existed");
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

    public List<KoiResponse> getKoiByBreed(Long breedId) {
        Breed breed = breedRepository.findBreedByIdAndIsDeletedFalse(breedId);
        if (breed == null) throw new NotFoundException("Breed not found");

        List<Koi> kois = koiLotRepository.findByBreedsAndIsDeletedFalseAndSoldFalse(breed);
        List<KoiResponse> koiLotResponses = new ArrayList<>();

        for (Koi koi : kois) {
            KoiResponse koiLotResponse = modelMapper.map(koi, KoiResponse.class);

            List<String> breedNames = koi.getBreeds().stream()
                    .filter(b -> !b.isDeleted())
                    .map(Breed::getName)
                    .collect(Collectors.toList());
            koiLotResponse.setBreeds(breedNames);

            List<String> imageUrls = koi.getImagesList().stream()
                    .map(Images::getImages)
                    .collect(Collectors.toList());
            koiLotResponse.setImagesList(imageUrls);

            if (koiLotResponse.getQuantity() == 1 && koi.getCertificate() != null) {
                String certificateImageUrls = koi.getCertificate().getImageUrl();
                koiLotResponse.setCertificate(certificateImageUrls);
            } else {
                koiLotResponse.setCertificate(null);
            }
            koiLotResponses.add(koiLotResponse);
        }
        return koiLotResponses;
    }

    public SaleRequest sale(SaleRequest saleRequest) {
        Koi koi = koiLotRepository.findKoiByIdAndIsDeletedFalse(saleRequest.getKoiId());
//        LocalDateTime now = LocalDateTime.now();
        koi.setSalePercentage(saleRequest.getSalePercentage());
//        if (now.isAfter(saleRequest.getSaleStartTime()) && now.isBefore(saleRequest.getSaleEndTime())) {
        float salePrice = koi.getPrice() - (koi.getPrice() * saleRequest.getSalePercentage() / 100);
        koi.setSalePrice(salePrice);
//        } else if (now.isAfter(saleRequest.getSaleEndTime())) {
//            koi.setSalePrice(0);
//            koi.setSalePercentage(0);
//        }
        koiLotRepository.save(koi);
        return saleRequest;
    }

    public void unSale(long id) {
        Koi koi = koiLotRepository.findById(id);
        koi.setSalePrice(0);
        koi.setSalePercentage(0);
        koiLotRepository.save(koi);
    }

    public List<Koi> getKoiByCurrentAccount() {
        return koiLotRepository.findByAccountId(authenticationService.getCurrentAccount().getId());
    }
}

