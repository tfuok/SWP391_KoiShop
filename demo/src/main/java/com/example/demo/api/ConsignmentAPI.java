package com.example.demo.api;

import com.example.demo.entity.Consignment;
import com.example.demo.model.Request.ConsignmentRequest;
import com.example.demo.model.Response.ConsignmentResponse;
import com.example.demo.model.Response.KoiOfflineConsignmentResponse;
import com.example.demo.model.Response.KoiOnlineConsignmentResponse;
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

    @PostMapping()
    public ResponseEntity createConsignment(@RequestBody ConsignmentRequest consignment) throws Exception {
        String url = consignmentService.createUrl(consignment);
        return ResponseEntity.ok(url);
    }


    @GetMapping("manager")
    public ResponseEntity<List<ConsignmentResponse>> showConsignments() {
        List<ConsignmentResponse> consignmentList = consignmentService.getAllConsignments();
        return ResponseEntity.ok(consignmentList);
    }

    @GetMapping("staff")
    public ResponseEntity<List<ConsignmentResponse>> getConsignment() {
        List<ConsignmentResponse> consignmentList = consignmentService.getStaffConsignments();
        return ResponseEntity.ok(consignmentList);
    }

    @PostMapping("transactions")
    public ResponseEntity create(@RequestParam long consignmentID) throws Exception {
        consignmentService.createConsignmentTransaction(consignmentID);
        return ResponseEntity.ok("success");
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Consignment> deleteConsignment(@PathVariable long id) {
        Consignment consignment = consignmentService.deleteConsignment(id);
        return ResponseEntity.ok(consignment);
    }

    @PutMapping("{id}")
    public ResponseEntity<Consignment> updateConsignment(@Valid @RequestBody ConsignmentRequest consignment, @PathVariable long id) {
        Consignment consignmentResponse = consignmentService.updateConsignment(consignment, id);
        return ResponseEntity.ok(consignmentResponse);
    }
    @GetMapping("getOnlineConsignmentKoi")
    public ResponseEntity<List<KoiOnlineConsignmentResponse>> getOnlineConsignmentKoi() {
        List<KoiOnlineConsignmentResponse> consignmentList = consignmentService.getAllOnlineKoi();
        return ResponseEntity.ok(consignmentList);
    }
    @GetMapping("getOfflineConsignmentKoi")
    public ResponseEntity<List<KoiOfflineConsignmentResponse>> getOfflineConsignmentKoi() {
        List<KoiOfflineConsignmentResponse> consignmentList = consignmentService.getAllOfflineKoi();
        return ResponseEntity.ok(consignmentList);
    }
}


