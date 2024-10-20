package com.example.demo.repository;

import com.example.demo.entity.Report;
import com.example.demo.model.Response.ReportResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    @Query("SELECT new com.example.demo.model.Response.ReportResponse(r.id, r.reportMessage, c.username, o.id) " +
            "FROM Report r " +
            "JOIN r.customer c " +  // Assuming there's a ManyToOne relationship between Report and Customer
            "JOIN r.orders o")      // Assuming there's a ManyToOne relationship between Report and Orders
    List<ReportResponse> findAllReportsWithDetails();
}
