package com.example.demo.repository;

import com.example.demo.entity.Consignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConsignmentRepository extends JpaRepository<Consignment, Long> {
    @Query("SELECT c FROM Consignment c WHERE c.id = :id ")
    Consignment findConsignmentById(@Param("id") long id);

    List<Consignment> findByIsDeletedFalse();
    @Query("SELECT c FROM Consignment c WHERE c.staff.id = :staffId AND c.isDeleted = false")
    List<Consignment> findByStaff_Id(@Param("staffId") long staffId);

    @Query("SELECT cd.consignment FROM ConsignmentDetails cd WHERE cd.koi.id = :koiId AND cd.consignment.isDeleted = false")
    Consignment findConsignmentByKoiId(@Param("koiId") Long koiId);

}
