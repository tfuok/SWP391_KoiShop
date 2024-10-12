package com.example.demo.api;

import com.example.demo.entity.CareType;
import com.example.demo.entity.Certificate;
import com.example.demo.exception.DuplicatedEntity;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.CareTypeRequest;
import com.example.demo.model.Request.CertificateRequest;
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
    @PostMapping
    public ResponseEntity<Certificate> createCertificate(@RequestBody CertificateRequest certificateRequest) {
        try {
            Certificate certificate = certificateService.createCertificate(certificateRequest);
            return ResponseEntity.ok(certificate);
        } catch (DuplicatedEntity e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<Certificate> updateCertificate(@RequestBody  CertificateRequest certificateRequest, @PathVariable long id) {
        try {
            Certificate certificate = certificateService.updateCertificate(id,certificateRequest);
            return ResponseEntity.ok(certificate);
        } catch (NotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

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
