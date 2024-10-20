package com.example.demo.service;

import com.example.demo.entity.Account;
import com.example.demo.entity.Feedback;
import com.example.demo.entity.Orders;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.FeedbackRequest;
import com.example.demo.model.Response.FeedbackResponse;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.FeedbackRepository;
import com.example.demo.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedbackService {
    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    FeedbackRepository feedbackRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    OrderRepository orderRepository;

    public Feedback createNewFeedback(FeedbackRequest feedbackRequest) {
        Feedback feedback = new Feedback();
//        Account shop = accountRepository.findById(feedbackRequest.getShopId())
//                .orElseThrow(() -> new EntityNotFoundException("Shop not found"));
        feedback.setContent(feedbackRequest.getContent());
        feedback.setRating(feedbackRequest.getRating());
        feedback.setCustomer(authenticationService.getCurrentAccount());
//        feedback.setShop(shop);
        return feedbackRepository.save(feedback);
    }

    public Feedback feedbackOnOrders(FeedbackRequest feedbackRequest, long orderId){
        Feedback feedback = new Feedback();
//        Account account = authenticationService.getCurrentAccount();
        Orders orders = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        feedback.setContent(feedbackRequest.getContent());
        feedback.setRating(feedbackRequest.getRating());
        feedback.setOrders(orders);
        feedback.setCustomer(orders.getCustomer());
        return feedbackRepository.save(feedback);
    }

    public List<FeedbackResponse> getFeedback() {
        return feedbackRepository.findAll().stream()
                .map(feedback -> {
                    Orders orders = feedback.getOrders();
                    Long orderId = null;

                    // Check if orders is not null, then retrieve orderId, otherwise log and continue
                    if (orders != null) {
                        orderId = orders.getId();
                    } else {
                        // You can log or handle the case where feedback is not associated with an order
                        System.out.println("Warning: Feedback " + feedback.getId() + " does not have an associated order.");
                    }

                    return new FeedbackResponse(
                            feedback.getId(),
                            feedback.getContent(),
                            feedback.getRating(),
                            feedback.getCustomer() != null ? feedback.getCustomer().getUsername() : null,
                            orderId  // Even if null, orderId will be included in the response
                    );
                })
                .collect(Collectors.toList());
    }

}
