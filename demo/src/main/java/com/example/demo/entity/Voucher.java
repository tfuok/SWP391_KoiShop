package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    private double discountValue;

    @Temporal(TemporalType.DATE)
    private Date createDate;

    @Temporal(TemporalType.DATE)
    private Date expiredDate;

    private int quantity;
    private int usedCount;

    private String description;

    private int minimumPoints; // Minimum points required to use the voucher

    private double minimumPrice; // Minimum order price required to use the voucher

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    private boolean isDeleted;
}