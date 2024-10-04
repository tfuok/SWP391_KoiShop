package com.example.demo.repository;

import com.example.demo.entity.Breed;
import com.example.demo.entity.Koi;
import com.example.demo.entity.KoiLot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KoiLotRepository extends JpaRepository<KoiLot, Long> {
    Page<KoiLot> findAllByIsDeletedFalse(Pageable pageable);

    KoiLot findKoiLotById(Long id);

    KoiLot findKoiLotByName(String name);

    List<KoiLot> findByBreeds(Breed breed);
}
