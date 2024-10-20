package com.example.demo.repository;

import com.example.demo.entity.Breed;
import com.example.demo.entity.Koi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KoiRepository extends JpaRepository<Koi, Long> {

    Page<Koi> findAllByIsDeletedFalse(Pageable pageable);

    Koi findKoiByIdAndIsDeletedFalse(Long id);

    Koi findByNameContainingAndIsDeletedFalse(String name);

    List<Koi> findByBreedsAndIsDeletedFalse(Breed breed);

    List<Koi> findByAccountId(Long accountId);

    List<Koi> findByAccountIdAndPriceIsNull(Long accountId);

    List<Koi> findByAccountIdAndPriceIsNotNull(Long accountId);

    // Find all Koi where they are not deleted and not sold
    Page<Koi> findAllByIsDeletedFalseAndSoldFalse(Pageable pageable);

    // Find Koi by name and ensure it's not deleted and not sold
    Koi findByNameContainingAndIsDeletedFalseAndSoldFalse(String name);

    // Find Koi by breed while ensuring they are not deleted and not sold
    List<Koi> findByBreedsAndIsDeletedFalseAndSoldFalse(Breed breed);

    Koi findById(long id);
}
