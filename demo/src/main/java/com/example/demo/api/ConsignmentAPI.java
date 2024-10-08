package com.example.demo.api;

import com.example.demo.entity.Consignment;
import com.example.demo.model.Request.ConsignmentCustomerRequest;
import com.example.demo.model.Request.ConsignmentRequest;

import com.example.demo.service.ConsignmentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consignment/")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class ConsignmentAPI {
    @Autowired
    ConsignmentService consignmentService;

    @PostMapping("create")
    public ResponseEntity createConsignment(@Valid @RequestBody ConsignmentRequest consignment) {
        Consignment consignmentRespone1 = consignmentService.createConsignment(consignment);
        return ResponseEntity.ok(consignmentRespone1);
    }
    @GetMapping("show")
    public ResponseEntity showConsignment() {
        List<Consignment> consignmentList = consignmentService.getAllConsignment();
        return ResponseEntity.ok(consignmentList);
    }
    @DeleteMapping("delete/{id}")
    public ResponseEntity deleteConsignment(@PathVariable long id) {
        Consignment consignment = consignmentService.deleteConsignment(id);
        return ResponseEntity.ok(consignment);
    }
    @PutMapping("update/{id}")
    public ResponseEntity updateConsignment(@Valid @RequestBody ConsignmentRequest consignment, @PathVariable long id) {
        Consignment consignmentRespone = consignmentService.updateConsignment(consignment, id);
        return ResponseEntity.ok(consignmentRespone);
    }
}
