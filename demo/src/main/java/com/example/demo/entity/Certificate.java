package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Certificate  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long certificateId;

    @ManyToOne
    @JoinColumn(name = "koi_id", nullable = false)
    private Koi koi;


    private String certificateCode;


    private LocalDate issueDate;


    private LocalDate expiryDate;


    private String healthStatus;


    private String awardCertificates;


    private String qrCode;


    private Date createdAt;


    private Date updatedAt;

    private  boolean isDeleted;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}

