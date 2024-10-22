package com.example.demo.api;

import com.example.demo.model.Request.OrderConsignmentRequest;
import com.example.demo.model.Request.OrderRequest;
import com.example.demo.service.ConsignmentOrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consignmentOder/")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class ConsignmentOrderAPI {
    @Autowired
    ConsignmentOrderService consignmentOrderService;
    @PostMapping
   public ResponseEntity create(@RequestBody OrderConsignmentRequest Order) throws Exception {
        String url = consignmentOrderService.createUrl(Order);
        return ResponseEntity.ok(url);
    }
    @PostMapping("/transaction")
    public ResponseEntity createTrans(@RequestParam long Orderid,@RequestParam long Consignmentid ) throws Exception {
        consignmentOrderService.createTransaction(Orderid,Consignmentid);
        return ResponseEntity.ok("Success");
    }
}
