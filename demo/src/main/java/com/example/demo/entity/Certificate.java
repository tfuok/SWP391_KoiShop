package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

    // Owning side of the relationship
    @OneToOne
    @JoinColumn(name = "koi_id", unique = true)
    @JsonBackReference("certificate")
    private Koi koi;

    private String certificateCode;
    private String storeName;
    private String variety;
    private String breeder;
    private int bornIn;
    private Date issueDate;
    private int size;
    private String imageUrl;
    private Date createdAt;
    private boolean isDeleted;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }


    }


