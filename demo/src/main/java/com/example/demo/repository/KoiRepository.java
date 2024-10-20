package com.example.demo.repository;

import com.example.demo.entity.Breed;
import com.example.demo.entity.Consignment;
import com.example.demo.entity.Koi;
import com.example.demo.entity.Type;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KoiRepository extends JpaRepository<Koi, Long> {

    Page<Koi> findAllByIsDeletedFalse(Pageable pageable);

    Koi findKoiByIdAndIsDeletedFalse(Long id);

    Koi findByNameContainingAndIsDeletedFalse(String name);

    List<Koi> findByBreedsAndIsDeletedFalse(Breed breed);

    List<Koi> findByAccountId(Long accountId);

    // Find all Koi by account ID through Consignment where Consignment type is ONLINE
    @Query("SELECT k FROM Koi k JOIN k.consignmentDetails cd JOIN cd.consignment c WHERE c.account.id = :accountId AND c.type = :type")
    List<Koi> findAllKoiByAccountIdAndConsignmentType(@Param("accountId") Long accountId, @Param("type") Type type);


    @Query("SELECT cd.consignment FROM ConsignmentDetails cd WHERE cd.koi.id = :koiId")
    Consignment findConsignmentByKoiId(@Param("koiId") Long koiId);

    Koi findById(long id);
}
