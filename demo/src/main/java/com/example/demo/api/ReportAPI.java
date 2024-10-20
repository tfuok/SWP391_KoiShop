package com.example.demo.api;

import com.example.demo.entity.Report;
import com.example.demo.model.Request.ReportRequest;
import com.example.demo.model.Response.ReportResponse;
import com.example.demo.service.FeedbackService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/report")
@CrossOrigin("*")//cho phép tất cả truy cập
@SecurityRequirement(name = "api")
public class ReportAPI {
    @Autowired
    FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity submitReport(@RequestBody ReportRequest reportRequest) {
        Report response = feedbackService.submitReport(reportRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity getAllReports() {
        List<ReportResponse> reports = feedbackService.getAllReports();
        return ResponseEntity.ok(reports);
    }
}
