package com.example.demo.service;

import com.example.demo.entity.Certificate;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.CertificateRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CertificateService {
    @Autowired
    private CertificateRepository certificateRepository;
    @Autowired
    private ModelMapper modelMapper;

    // Tạo mới Certificate
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
