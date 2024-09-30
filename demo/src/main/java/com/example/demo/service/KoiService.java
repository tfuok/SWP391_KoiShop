package com.example.demo.service;

import com.example.demo.entity.Account;
import com.example.demo.entity.Koi;
import com.example.demo.exception.DuplicatedEntity;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.KoiRequest;
import com.example.demo.repository.KoiRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KoiService {
    @Autowired
    KoiRepository koiRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    AuthenticationService authenticationService;

    public Koi createKoi(KoiRequest koiRequest) {
        try {
            Koi koi = modelMapper.map(koiRequest, Koi.class);
            //xac dinh manager nao tao ra ca koi nay
            Account accountRequest = authenticationService.getCurrentAccount();
            koi.setAccount(accountRequest);
            Koi newKoi = koiRepository.save(koi);
            return newKoi;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Koi> getAllKoi() {
        List<Koi> kois = koiRepository.findKoiByIsDeletedFalse();
        return kois;
    }

    public Koi updateKoi(KoiRequest koiRequest, long id) {
        //b1: tìm tới student có id như FE cung cấp
        Koi foundKoi = koiRepository.findKoiById(id);
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
        foundKoi.setBreed(koiRequest.getBreed());
        foundKoi.setOrigin(koiRequest.getOrigin());
        foundKoi.setDescription(koiRequest.getDescription());
        return koiRepository.save(foundKoi);
    }

    public Koi deleteKoi(long id) {
        Koi koi1 = koiRepository.findKoiById(id);
        if (koi1 == null) {
            throw new NotFoundException("Koi not found!");
        }
        koi1.setDeleted(true);
        return koiRepository.save(koi1);
    }

    public Koi searchByName(String name) {
        Koi koi = koiRepository.findKoiByName(name);
        if (koi == null) throw new NotFoundException("Koi not existed");
        return koi;
    }

    public Map<String, Object> compareKoi(long id1, long id2) {
        Koi koi1 = koiRepository.findKoiById(id1);
        Koi koi2 = koiRepository.findKoiById(id2);

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
}
