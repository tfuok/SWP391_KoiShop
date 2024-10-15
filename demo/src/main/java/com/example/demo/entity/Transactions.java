package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Transactions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "from_id")
    Account from;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "to_id")
    Account to;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "payment_id")
    Payment payment;


    TransactionEnum status;

    String description;
}
