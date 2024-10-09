package com.example.demo.service;

import com.example.demo.entity.Account;
import com.example.demo.entity.Breed;
import com.example.demo.entity.Koi;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.KoiRequest;
import com.example.demo.model.Response.KoiPageResponse;
import com.example.demo.model.Response.KoiResponse;
import com.example.demo.repository.BreedRepository;
import com.example.demo.repository.KoiRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KoiService {
    @Autowired
    KoiRepository koiRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    BreedRepository breedRepository;

    public Koi createKoi(KoiRequest koiRequest) {
        try {
//            // Map the request to the Koi entity
//            Koi koi = modelMapper.map(koiRequest, Koi.class);
//
            Koi koi = new Koi();
            koi.setName(koiRequest.getName());
            koi.setPrice(koiRequest.getPrice());
            koi.setVendor(koiRequest.getVendor());
            koi.setGender(koiRequest.getGender());
            koi.setBornYear(koiRequest.getBornYear());
            koi.setSize(koiRequest.getSize());
            koi.setOrigin(koiRequest.getOrigin());
            koi.setDescription(koiRequest.getDescription());
            koi.setImageUrl(koiRequest.getImageUrl());

            // Find the breed by breedId and ensure it's not deleted
            Breed breed = breedRepository.findBreedByIdAndIsDeletedFalse(koiRequest.getBreedId());
            if (breed == null) throw new NotFoundException("Breed not found");
            koi.setBreed(breed);

            // Identify the current account (manager) creating the Koi
            Account accountRequest = authenticationService.getCurrentAccount();
            koi.setAccount(accountRequest);

            // Save the new Koi
            Koi newKoi = koiRepository.save(koi);
            return newKoi;

        } catch (NotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error occurred while creating Koi.");
        }
    }


    public Koi updateKoi(KoiRequest koiRequest, long id) {
        //b1: tìm tới student có id như FE cung cấp
        Koi foundKoi = koiRepository.findKoiByIdAndIsDeletedFalse(id);
        if (foundKoi == null) {
            throw new NotFoundException("Koi not found");
        }
        //=> tồn tại
        foundKoi.setName(koiRequest.getName());
        foundKoi.setPrice(koiRequest.getPrice());
        foundKoi.setVendor(koiRequest.getVendor());
        foundKoi.setGender(koiRequest.getGender());
        foundKoi.setBornYear(koiRequest.getBornYear());
        foundKoi.setSize(koiRequest.getSize());
        foundKoi.setOrigin(koiRequest.getOrigin());
        foundKoi.setDescription(koiRequest.getDescription());

        Breed breed = breedRepository.findBreedByIdAndIsDeletedFalse(koiRequest.getBreedId());
        if (breed == null) throw new NotFoundException("Breed not found");
        foundKoi.setBreed(breed);
        return koiRepository.save(foundKoi);
    }

    public Koi deleteKoi(long id) {
        Koi koi1 = koiRepository.findKoiByIdAndIsDeletedFalse(id);
        if (koi1 == null) throw new NotFoundException("Koi not found!");
        koi1.setDeleted(true);
        return koiRepository.save(koi1);
    }


    public Map<String, Object> compareKoi(long id1, long id2) {
        Koi koi1 = koiRepository.findKoiByIdAndIsDeletedFalse(id1);
        Koi koi2 = koiRepository.findKoiByIdAndIsDeletedFalse(id2);

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
        comparisonResult.put("breedMatch", koi1.getBreed().equals(koi2.getBreed()));
        comparisonResult.put("originMatch", koi1.getOrigin().equals(koi2.getOrigin()));

        return comparisonResult;
    }

    public List<KoiResponse> getKoiByBreed(Long breedId) {
        Breed breed = breedRepository.findBreedByIdAndIsDeletedFalse(breedId);
        if (breed == null) throw new NotFoundException("Breed not found");
        // Retrieve all Koi by breed
        List<Koi> koiList = koiRepository.findByBreedAndIsDeletedFalse(breed);
        return koiList.stream()
                .map(koi -> {
                    KoiResponse koiResponse = modelMapper.map(koi, KoiResponse.class);
                    koiResponse.setBreedName(koi.getBreed().getName());  // Set breed name
                    return koiResponse;
                })
                .collect(Collectors.toList());
    }

    public List<KoiResponse> searchByName(String name) {
        List<Koi> koiList = koiRepository.findByNameContainingAndIsDeletedFalse(name);

        // Log the result size
        System.out.println("Number of Koi fetched: " + koiList.size());

        if (koiList.isEmpty()) {
            throw new NotFoundException("No Koi found with the specified name");
        }

        return koiList.stream()
                .map(koi -> modelMapper.map(koi, KoiResponse.class))
                .collect(Collectors.toList());
    }

    public KoiPageResponse getAllKoi(int page, int size) {
        Page<Koi> koiPage = koiRepository.findAllByIsDeletedFalse(PageRequest.of(page, size));
        // Map List<Koi> to List<KoiResponse>
        List<KoiResponse> koiResponseList = koiPage.getContent().stream()
                .map(koi -> {
                    KoiResponse koiResponse = modelMapper.map(koi, KoiResponse.class);
                    koiResponse.setBreedName(koi.getBreed().getName());  // Set breed name
                    return koiResponse;
                })
                .collect(Collectors.toList());
        // Populate KoiPageResponse with the results
        KoiPageResponse koiPageResponse = new KoiPageResponse();
        koiPageResponse.setContent(koiResponseList);  // Set the list of KoiResponse objects
        koiPageResponse.setPageNumber(koiPage.getNumber());
        koiPageResponse.setTotalElements(koiPage.getTotalElements());
        koiPageResponse.setTotalPages(koiPage.getTotalPages());

        return koiPageResponse;
    }

}
