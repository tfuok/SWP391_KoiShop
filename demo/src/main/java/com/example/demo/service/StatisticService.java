package com.example.demo.service;

import com.example.demo.entity.Account;
import com.example.demo.entity.Role;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.KoiRepository;
import com.example.demo.repository.TransactionRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.security.auth.message.AuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticService {
    @Autowired
    KoiRepository koiRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    AuthenticationService authenticationService;

    public Map<String, Object> getDashboardStats() {
        //dem so san pham trong he thong
        Map<String, Object> stats = new HashMap<>();
        long totalProducts = koiRepository.count();
        stats.put("totalProducts", totalProducts);
        //so luong customer
        long customerCount = accountRepository.countByRole(Role.CUSTOMER);
        stats.put("CUSTOMER", customerCount);
//        //so luong staff
        long ownerCount = accountRepository.countByRole(Role.STAFF);
        stats.put("STAFF", ownerCount);
        //top 5 san pham ban chay nhat
        List<Object[]> topBreeds = koiRepository.findTopBreeds();
        List<Map<String, Object>> topBreedsList = new ArrayList<>();
        for (Object[] objects : topBreeds) {
            Map<String, Object> breedInfo = new HashMap<>();
            breedInfo.put("BreedName", objects[0]);
            breedInfo.put("TotalSold", objects[1]);
            topBreedsList.add(breedInfo);
        }
        stats.put("topBreeds", topBreedsList);
        return stats;

    }

    public Map<String, Object> getMonthlyRevenue() throws AuthException {
        Map<String, Object> revenueData = new HashMap<>();
        Account account = authenticationService.getCurrentAccount();
        if (account == null) throw new AuthException("Login first");
        List<Object[]> monthlyRevenue = transactionRepository.
                calculateRevenue(account.getId());
        List<Map<String, Object>> monthlyRevenueList = new ArrayList<>();
        for (Object[] res : monthlyRevenue) {
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("Year", res[0]);
            monthData.put("Month", res[1]);
            monthData.put("Total", res[2]);
            monthlyRevenueList.add(monthData);
        }
        revenueData.put("RevenueData", monthlyRevenueList);
        return revenueData;
    }

}
