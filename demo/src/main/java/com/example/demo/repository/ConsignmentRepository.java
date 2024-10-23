package com.example.demo.repository;

import com.example.demo.entity.Consignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConsignmentRepository extends JpaRepository<Consignment, Long> {
    Consignment findConsignmentById(long id);
    List<Consignment> findByIsDeletedFalse();
    List<Consignment> findByStaff_Id(long staffId);
    @Query("SELECT cd.consignment FROM ConsignmentDetails cd WHERE cd.koi.id = :koiId")
    Consignment findConsignmentByKoiId(@Param("koiId") Long koiId);
}
