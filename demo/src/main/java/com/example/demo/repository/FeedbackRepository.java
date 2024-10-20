package com.example.demo.repository;

import com.example.demo.entity.Feedback;
import com.example.demo.model.Response.FeedbackResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    @Query("SELECT new com.example.demo.model.Response.FeedbackResponse(f.id, f.content, f.rating, " +
            "CASE WHEN f.customer IS NOT NULL THEN f.customer.username ELSE NULL END, o.id) " +
            "FROM Feedback f " +
            "LEFT JOIN f.customer c " +
            "JOIN f.orders o")
    List<FeedbackResponse> findAllFeedbackWithDetails();
}

