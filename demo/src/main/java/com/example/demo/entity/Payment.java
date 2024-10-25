package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    double total;

    @Enumerated(EnumType.STRING)
    PaymentEnums method;

    @OneToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore
    Orders orders;

    @OneToOne
    @JoinColumn(name = "consignment_id")
    Consignment consignment;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
    @JsonIgnore
    List<Transactions> transactions;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    @JsonIgnore
    Account customer;
}
