package com.example.demo.api;

import com.example.demo.entity.Voucher;
import com.example.demo.model.Request.VoucherRequest;
import com.example.demo.service.VoucherService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/voucher")
@CrossOrigin("*")
@SecurityRequirement(name = "api") //bat buoc phai co
public class VoucherAPI {
    @Autowired
    VoucherService voucherService;

    @PostMapping
    public ResponseEntity create(@Valid @RequestBody VoucherRequest voucherRequest) {
        Voucher voucher = voucherService.createVoucher(voucherRequest);
        return ResponseEntity.ok(voucher);
    }

    @DeleteMapping("{id}")
    public ResponseEntity delete(long id) {
        Voucher voucher = voucherService.deleteVoucher(id);
        return ResponseEntity.ok(voucher);
    }

    @GetMapping
    public ResponseEntity getAllVoucher() {
        List<Voucher> vouchers = voucherService.getAllVoucher();
        return ResponseEntity.ok(vouchers);
    }

    @PutMapping("{id}")
    public ResponseEntity updateVoucher(@Valid @RequestBody VoucherRequest voucherRequest, @PathVariable long id) {
        Voucher voucher = voucherService.updateVoucher(voucherRequest, id);
        return ResponseEntity.ok(voucher);
    }
}
