package com.example.demo.api;

import com.example.demo.entity.Consignment;
import com.example.demo.entity.ConsignmentStatus;
import com.example.demo.entity.Orders;
import com.example.demo.entity.Status;
import com.example.demo.model.Request.ConsignmentRequest;
import com.example.demo.model.Response.ConsignmentResponse;
import com.example.demo.model.Response.KoiOfflineConsignmentResponse;
import com.example.demo.model.Response.KoiOnlineConsignmentResponse;
import com.example.demo.service.ConsignmentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/consignment")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@Validated
public class ConsignmentAPI {

    @Autowired
    private ConsignmentService consignmentService;

    @PostMapping()
    public ResponseEntity createConsignment(@Valid @RequestBody ConsignmentRequest consignment) throws Exception {
        String url = consignmentService.createUrl(consignment);
        return ResponseEntity.ok(url);
    }
    @GetMapping("/manager")
    public ResponseEntity<List<ConsignmentResponse>> showConsignments() {
        List<ConsignmentResponse> consignmentList = consignmentService.getAllConsignments();
        return ResponseEntity.ok(consignmentList);
    }

    @GetMapping("/staff")
    public ResponseEntity<List<ConsignmentResponse>> getConsignment() {
        List<ConsignmentResponse> consignmentList = consignmentService.getStaffConsignments();
        return ResponseEntity.ok(consignmentList);
    }
    @PutMapping("/assign-staff")
    public ResponseEntity assignStaff(long consignmentId, long staffId){
        ConsignmentResponse consignmentResponse = consignmentService.assignStaff(consignmentId,staffId);
        return ResponseEntity.ok(consignmentResponse);
    }
    @PostMapping("/transactions")
    public ResponseEntity create(@RequestParam long consignmentID) throws Exception {
        consignmentService.createConsignmentTransaction(consignmentID);
        return ResponseEntity.ok("success");
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Consignment> deleteConsignment(@PathVariable long id) {
        Consignment consignment = consignmentService.deleteConsignment(id);
        return ResponseEntity.ok(consignment);
    }
    @GetMapping("/getOnlineConsignmentKoi")
    public ResponseEntity<List<KoiOnlineConsignmentResponse>> getOnlineConsignmentKoi() {
        List<KoiOnlineConsignmentResponse> consignmentList = consignmentService.getAllOnlineKoi();
        return ResponseEntity.ok(consignmentList);
    }
    @GetMapping("/getOfflineConsignmentKoi")
    public ResponseEntity<List<KoiOfflineConsignmentResponse>> getOfflineConsignmentKoi() {
        List<KoiOfflineConsignmentResponse> consignmentList = consignmentService.getAllOfflineKoi();
        return ResponseEntity.ok(consignmentList);
    }
    @PutMapping("/status")
    public ResponseEntity<Consignment> updateConsignmentStatus(
           long consignmentId, ConsignmentStatus newStatus) throws Exception {
        Consignment updatedConsignment  = consignmentService.updateConsignmentStatus(consignmentId, newStatus);
        return ResponseEntity.ok(updatedConsignment);
    }
    @PutMapping("extend")
    public ResponseEntity extendConsignment(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date extendDate,
            @RequestParam Long id) throws Exception {
        String url = consignmentService.createExtendUrl(id, extendDate);
        return ResponseEntity.ok(url);
    }
    @PostMapping("/extendTransactions")
    public ResponseEntity extendTransactions(@RequestParam long consignmentID) throws Exception {
        consignmentService.createExtendTransaction(consignmentID);
        return ResponseEntity.ok("success");
    }
    @PutMapping("/cancel")
    public ResponseEntity<Consignment> cancelOrder(@RequestParam Long consginementid) throws Exception {
        Consignment cancelledOrder = consignmentService.cancelConsignmentByCustomer(consginementid);
        return ResponseEntity.ok(cancelledOrder);
    }
}


