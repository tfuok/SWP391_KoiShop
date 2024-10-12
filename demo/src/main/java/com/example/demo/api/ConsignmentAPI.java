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
    private ConsignmentService consignmentService;

    @PostMapping("create")
    public ResponseEntity<Consignment> createConsignment(@Valid @RequestBody ConsignmentRequest consignment) {
        Consignment consignmentResponse = consignmentService.createConsignment(consignment);
        return ResponseEntity.ok(consignmentResponse);
    }

    @GetMapping("show")
    public ResponseEntity<List<Consignment>> showConsignments() {
        List<Consignment> consignmentList = consignmentService.getAllConsignment();
        return ResponseEntity.ok(consignmentList);
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<Consignment> deleteConsignment(@PathVariable long id) {
        Consignment consignment = consignmentService.deleteConsignment(id);
        return ResponseEntity.ok(consignment);
    }

    @PutMapping("update/{id}")
    public ResponseEntity<Consignment> updateConsignment(@Valid @RequestBody ConsignmentRequest consignment, @PathVariable long id) {
        Consignment consignmentResponse = consignmentService.updateConsignment(consignment, id);
        return ResponseEntity.ok(consignmentResponse);
    }
}
