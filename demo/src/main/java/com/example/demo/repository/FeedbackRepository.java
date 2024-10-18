package com.example.demo.repository;

import com.example.demo.entity.Feedback;
import com.example.demo.model.Response.FeedbackResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
//    @Query("SELECT new com.example.demo.model.Response.FeedbackResponse(f.id, f.content, f.rating, a.email) " +
//            "FROM Feedback f JOIN Account a ON f.shop.id = a.id WHERE f.shop.id = :shopID")
//    List<FeedbackResponse> findFeedbackByShopId();
}

