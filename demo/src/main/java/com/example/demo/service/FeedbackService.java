package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.FeedbackRequest;
import com.example.demo.model.Request.ReportRequest;
import com.example.demo.model.Response.FeedbackResponse;
import com.example.demo.model.Response.ReportResponse;
import com.example.demo.repository.*;
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
    @Autowired
    ReportRepository reportRepository;
    @Autowired
    private ConsignmentRepository consignmentRepository;

    public Feedback feedbackOnOrders(FeedbackRequest feedbackRequest, long orderId) {
        Feedback feedback = new Feedback();
        Orders orders = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        feedback.setContent(feedbackRequest.getContent());
        feedback.setRating(feedbackRequest.getRating());
        feedback.setOrders(orders);
        feedback.setCustomer(orders.getCustomer());
        return feedbackRepository.save(feedback);
    }
    public Feedback feedbackOnOrderConsignment(FeedbackRequest feedbackRequest, long orderId, long consignmentId) {
        // Check if feedback already exists for the order
        if (feedbackRepository.existsByOrders_Id(orderId)) {
            throw new IllegalStateException("Feedback already exists for this order");
        }

        // Check if feedback already exists for the consignment
        if (feedbackRepository.existsByConsignment_Id(consignmentId)) {
            throw new IllegalStateException("Feedback already exists for this consignment");
        }

        Feedback feedback = new Feedback();
        Orders orders = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        Consignment consignment = consignmentRepository.findById(consignmentId)
                .orElseThrow(() -> new NotFoundException("Consignment not found"));

        feedback.setConsignment(consignment);
        feedback.setContent(feedbackRequest.getContent());
        feedback.setRating(feedbackRequest.getRating());
        feedback.setOrders(orders);
        feedback.setCustomer(orders.getCustomer());

        return feedbackRepository.save(feedback);
    }



    public List<FeedbackResponse> getFeedback() {
        return feedbackRepository.findAllFeedbackWithDetails();
    }

    public Report submitReport(ReportRequest reportRequest) {
        Account customer = authenticationService.getCurrentAccount();
        Orders order = orderRepository.findByIdAndCustomer(reportRequest.getOrderId(), customer);

        Report report = new Report();
        report.setReportMessage(reportRequest.getReportMessage());
        report.setCustomer(customer);
        report.setOrders(order);
        return reportRepository.save(report);
    }

    public List<ReportResponse> getAllReports() {
        return reportRepository.findAllReportsWithDetails();
    }


    //    public Feedback createNewFeedback(FeedbackRequest feedbackRequest) {
//        Feedback feedback = new Feedback();
////        Account shop = accountRepository.findById(feedbackRequest.getShopId())
////                .orElseThrow(() -> new EntityNotFoundException("Shop not found"));
//        feedback.setContent(feedbackRequest.getContent());
//        feedback.setRating(feedbackRequest.getRating());
//        feedback.setCustomer(authenticationService.getCurrentAccount());
////        feedback.setShop(shop);
//        return feedbackRepository.save(feedback);
//    }

//    public List<ReportResponse> getAllReports() {
//        List<Report> reports = reportRepository.findAll();
//        return reports.stream()
//                .map(report -> new ReportResponse(
//                        report.getId(),
//                        report.getReportMessage(),
//                        report.getCustomer().getUsername(),
//                        report.getOrders().getId()
//                ))
//                .collect(Collectors.toList());
//    }

    //    public List<FeedbackResponse> getFeedback() {
//        return feedbackRepository.findAll().stream()
//                .map(feedback -> {
//                    Orders orders = feedback.getOrders();
//                    return new FeedbackResponse(
//                            feedback.getId(),
//                            feedback.getContent(),
//                            feedback.getRating(),
//                            feedback.getCustomer() != null ? feedback.getCustomer().getUsername() : null,
//                            orders.getId()
//                    );
//                })
//                .collect(Collectors.toList());
//    }
}
