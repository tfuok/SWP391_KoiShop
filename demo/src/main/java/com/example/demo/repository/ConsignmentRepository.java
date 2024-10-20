package com.example.demo.repository;

import com.example.demo.entity.Consignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsignmentRepository extends JpaRepository<Consignment, Long> {
    Consignment findConsignmentById(long id);
    List<Consignment> findByIsDeletedFalse();
    List<Consignment> findByStaff_Id(long staffId);
}
