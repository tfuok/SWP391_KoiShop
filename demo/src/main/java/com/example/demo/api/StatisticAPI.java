package com.example.demo.api;

import com.example.demo.service.StatisticService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.security.auth.message.AuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@SecurityRequirement(name = "api")
public class StatisticAPI {
    @Autowired
    StatisticService statisticService;

    @GetMapping("/stats")
    public ResponseEntity getDashboard(){
        Map<String, Object> stats = statisticService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/revenue/monthly")
    public ResponseEntity getMonthlyRevenue() throws AuthException {
        Map<String, Object> revenue = statisticService.getMonthlyRevenue();
        return ResponseEntity.ok(revenue);
    }
}
