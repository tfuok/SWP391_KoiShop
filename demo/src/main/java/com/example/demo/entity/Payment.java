package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    Date createAt;

    @Enumerated(EnumType.STRING)
    PaymentEnums method;

    @OneToOne
    @JoinColumn(name = "order_id")
    Orders orders;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
    List<Transactions> transactions;
}
