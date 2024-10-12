package com.example.demo.model.Request;

import lombok.Data;

import java.time.LocalDate;
@Data
public class CertificateRequest {
    public long certificateID;
    private String certificateCode;


    private LocalDate issueDate;


    private LocalDate expiryDate;


    private String healthStatus;


    private String awardCertificates;

}
