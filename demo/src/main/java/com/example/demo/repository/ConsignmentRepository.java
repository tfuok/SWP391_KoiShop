package com.example.demo.repository;

import com.example.demo.entity.Consignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsignmentRepository extends JpaRepository<Consignment, Long> {
    List<Consignment> findConsignmentByIsDeletedFalse();
    Consignment findConsignmentByconsignmentID(long id);
}
