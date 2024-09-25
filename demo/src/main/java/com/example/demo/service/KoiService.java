package com.example.demo.service;

import com.example.demo.entity.Koi;
import com.example.demo.exception.DuplicatedEntity;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.KoiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KoiService {
    @Autowired
    KoiRepository koiRepository;

    public Koi createKoi(Koi koi) {
        try {
            Koi newKoi = koiRepository.save(koi);
            return newKoi;
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().contains(koi.getKoiCode())) {
                throw new DuplicatedEntity("Duplicated code");
            }
        }
        return null;
    }

    public List<Koi> getAllKoi() {
        List<Koi> kois = koiRepository.findKoiByIsDeletedFalse();
        return kois;
    }

    public Koi updateKoi(Koi koi, long id) {
        //b1: tìm tới student có id như FE cung cấp
        Koi foundKoi = koiRepository.findKoiById(id);
        if (foundKoi == null) {
            throw new NotFoundException("Student not found");
        }
        //=> tồn tại
        foundKoi.setKoiCode(koi.getKoiCode());
        foundKoi.setName(koi.getName());
        foundKoi.setPrice(koi.getPrice());
        foundKoi.setVendor(koi.getVendor());
        foundKoi.setGender(koi.getGender());
        foundKoi.setBornYear(koi.getBornYear());
        foundKoi.setSize(koi.getSize());
        foundKoi.setBreed(koi.getBreed());
        foundKoi.setOrigin(koi.getOrigin());
        foundKoi.setDescription(koi.getDescription());
//        foundKoi.setSold(koi.gets);
        return koiRepository.save(foundKoi);
    }

    public Koi deleteKoi(Koi koi, long id) {
        Koi koi1 = koiRepository.findKoiById(id);
        if (koi1 == null) {
            throw new NotFoundException("Koi not found!");
        }
        koi1.setDeleted(true);
        return koiRepository.save(koi1);
    }
}
