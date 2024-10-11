package com.example.demo.repository;

import com.example.demo.entity.Breed;
import com.example.demo.entity.Koi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KoiRepository extends JpaRepository<Koi, Long> {

    Koi findKoiByIdAndIsDeletedFalse(long id);

    List<Koi> findKoiByIsDeletedFalse();

    List<Koi> findByNameContainingAndIsDeletedFalse(String name);

    List<Koi> findByBreedAndIsDeletedFalse(Breed breed);

    Page<Koi> findAllByIsDeletedFalse(Pageable pageable);

    List<Koi> findByAccountId(long accountId);
}
