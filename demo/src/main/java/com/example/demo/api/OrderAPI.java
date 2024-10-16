package com.example.demo.api;

import com.example.demo.entity.Account;
import com.example.demo.entity.Orders;
import com.example.demo.model.Request.OrderRequest;
import com.example.demo.repository.OrderRepository;
import com.example.demo.service.AuthenticationService;
import com.example.demo.service.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/order")
@CrossOrigin("*")//cho phép tất cả truy cập
@SecurityRequirement(name = "api")
public class OrderAPI {
    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepo;

    @Autowired
    AuthenticationService authenticationService;
    @PostMapping
    public ResponseEntity create(@RequestBody OrderRequest orderRequest) throws Exception {
        String url = orderService.createUrl(orderRequest);
        return ResponseEntity.ok(url);
    }

    @GetMapping
    public ResponseEntity get(){
        Account account = authenticationService.getCurrentAccount();
        List<Orders> orders = orderRepo.findOrderssByCustomer(account);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/transaction")
    public ResponseEntity createTrans(@RequestParam long id) throws Exception {
         orderService.createTransaction(id);
        return ResponseEntity.ok("Success");
    }
}
