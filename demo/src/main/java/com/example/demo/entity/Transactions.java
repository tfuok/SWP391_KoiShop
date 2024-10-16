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
    @JoinColumn(name = "from_id")
    @JsonIgnore

    Account from;

    @ManyToOne
    @JoinColumn(name = "to_id")
    @JsonIgnore

    Account to;

    @ManyToOne
    @JoinColumn(name = "payment_id")
    @JsonIgnore

    Payment payment;


    TransactionEnum status;

    String description;
}
