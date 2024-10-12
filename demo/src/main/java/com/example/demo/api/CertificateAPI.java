package com.example.demo.api;

import com.example.demo.entity.Certificate;
import com.example.demo.exception.NotFoundException;
import com.example.demo.service.CertificateService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificate")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class CertificateAPI {
    @Autowired
    private CertificateService certificateService;


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCareType(@PathVariable long id) {
        try {
            certificateService.deleteCertificate(id);
            return ResponseEntity.ok().build();
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Certificate>> getAllCareType() {
        try {
            List<Certificate> certificates = certificateService.getAllCertificates();
            return ResponseEntity.ok(certificates);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}
