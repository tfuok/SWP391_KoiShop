package com.example.demo.api;

import com.example.demo.entity.Feedback;
import com.example.demo.entity.Koi;
import com.example.demo.model.Request.FeedbackRequest;
import com.example.demo.model.Response.FeedbackResponse;
import com.example.demo.service.FeedbackService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin("*")//cho phép tất cả truy cập
@SecurityRequirement(name = "api")
public class FeedbackAPI {
    @Autowired
    FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity create(@RequestBody FeedbackRequest feedbackRequest) {
        Feedback feedback = feedbackService.createNewFeedback(feedbackRequest);
        return ResponseEntity.ok(feedback);
    }

    @GetMapping
    public ResponseEntity getFeedback() {
        List<FeedbackResponse> feedback = feedbackService.getFeedback();
        return ResponseEntity.ok(feedback);
    }
}