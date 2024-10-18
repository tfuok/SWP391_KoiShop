package com.example.demo.service;

import com.example.demo.entity.Account;
import com.example.demo.entity.Feedback;
import com.example.demo.model.Request.FeedbackRequest;
import com.example.demo.model.Response.FeedbackResponse;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.FeedbackRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackService {
    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    FeedbackRepository feedbackRepository;
    @Autowired
    AccountRepository accountRepository;

    public Feedback createNewFeedback(FeedbackRequest feedbackRequest) {
        Feedback feedback = new Feedback();
        Account shop = accountRepository.findById(feedbackRequest.getShopId())
                .orElseThrow(() -> new EntityNotFoundException("Shop not found"));
        feedback.setContent(feedbackRequest.getContent());
        feedback.setRating(feedbackRequest.getRating());
        feedback.setCustomer(authenticationService.getCurrentAccount());
        feedback.setShop(shop);
        return feedbackRepository.save(feedback);
    }

    public List<FeedbackResponse> getFeedback() {
        return feedbackRepository.findFeedbackByShopId(authenticationService.getCurrentAccount().getId());
    }
}
