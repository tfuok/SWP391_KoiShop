package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    float discountValue;
    Date createDate = new Date();
    Date expiredDate;
    int quantity;
    int usedCount;
    String description;
    int minimumPoints; // diem tich luy toi thieu de co the su dung dc voucher
    float minimumPrice; //giá đơn hàng tối thieu de co the su dung dc voucher
    @ManyToOne
    @JoinColumn(name = "account_id")
    Account account;
    boolean isDeleted = false;
}
