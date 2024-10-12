package com.example.demo.service;

import com.example.demo.entity.Certificate;
import com.example.demo.entity.Consignment;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.CertificateRequest;
import com.example.demo.repository.CertificateRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
public class CertificateService {
    @Autowired
    private CertificateRepository certificateRepository;
    @Autowired
    private ModelMapper modelMapper;

    // Tạo mới Certificate
    public Certificate createCertificate(CertificateRequest certificateRequest) {
        Certificate certificate = modelMapper.map(certificateRequest, Certificate.class);
        certificate.setCreatedAt(new Date());
        certificate.setUpdatedAt(new Date());
        return certificateRepository.save(certificate);
    }

    // Lấy tất cả Certificates
    public List<Certificate> getAllCertificates() {
        return certificateRepository.findAll();
    }

    // Lấy Certificate theo ID
    public Certificate getCertificateById(Long id) {
        Certificate certificateFound = certificateRepository.findCertificateByCertificateId(id);
        if (certificateFound == null) {
            throw new NotFoundException("certificate not found!");
        }
        return certificateFound;
    }

    // Cập nhật Certificate
    public Certificate updateCertificate(Long id, CertificateRequest certificateRequest) {
        Certificate certificateFound = certificateRepository.findCertificateByCertificateId(id);
        if (certificateFound == null) {
            throw new NotFoundException("certificate not found!");
        }
        certificateFound.setCertificateCode(certificateRequest.getCertificateCode());
        certificateFound.setIssueDate(certificateRequest.getIssueDate());
        certificateFound.setExpiryDate(certificateRequest.getExpiryDate());
        certificateFound.setAwardCertificates(certificateRequest.getAwardCertificates());
        certificateFound.setUpdatedAt(new Date());
        return certificateRepository.save(certificateFound);
    }

    // Xóa Certificate
    public void deleteCertificate(Long id) {
        Certificate certificateFound = certificateRepository.findCertificateByCertificateId(id);
        if (certificateFound == null) {
            throw new NotFoundException("certificate not found!");
        }
        certificateFound.setDeleted(true);
        certificateRepository.save(certificateFound);
    }
}
