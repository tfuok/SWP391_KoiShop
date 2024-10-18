package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.EmailDetails;
import com.example.demo.model.Request.KoiRequest;
import com.example.demo.repository.CertificateRepository;
import com.example.demo.repository.KoiRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.List;

@Service
public class CertificateService {
    @Autowired
    private CertificateRepository certificateRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private KoiRepository koiRepository;
    @Autowired
    private CertificatePdfGeneratorService certificatePdfGeneratorService;
    @Autowired
    private EmailService emailService;
    public Certificate createCertificates(Koi koi) {
//        for (OrderDetails orderDetail : orders.getOrderDetails()) {
//            Koi koi = orderDetail.getKoi();

            // Create and save certificate for each Koi
            Certificate certificate = new Certificate();
            certificate.setKoi(koi);
//            certificate.setVariety(koi.getName());
//            certificate.setBreeder(koi.getVendor());
//            certificate.setBornIn(koi.getBornYear());
//            certificate.setSize(koi.getSize());
            certificate.setImageUrl(koi.getImages());
            certificate.setIssueDate(new Date());

            certificateRepository.save(certificate);
            koi.setCertificate(certificate);
            koiRepository.save(koi);
            return certificate;
            // Send certificate email
          //  sendCertificateEmail(orders.getCustomer(), certificate);
       // }
    }
    public void sendCertificateEmail(Account customer, Certificate certificate) {
        try {
            // Generate PDF from HTML
            File pdfFile = certificatePdfGeneratorService.createCertificatePdf(certificate);

            // Prepare email details
            EmailDetails emailDetails = new EmailDetails();
            emailDetails.setReceiver(customer);
            emailDetails.setSubject("Your Koi Certificate");
            emailDetails.setLink("http://koishop.site/");

            // Send the email with the PDF attachment
            emailService.sendEmailWithAttachment(emailDetails, pdfFile);
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exception (log or retry)
        }
    }

    // Lấy tất cả Certificates
    public List<Certificate> getAllCertificates() {
        return certificateRepository.findAll();
    }
    // Lấy Certificate theo ID
    public String getCertificateById(Long id) {
        Certificate certificateFound = certificateRepository.findCertificateByCertificateId(id);
        if (certificateFound == null) {
            throw new NotFoundException("certificate not found!");
        }
        String imageUrl =certificateFound.getImageUrl();
        return imageUrl;
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
