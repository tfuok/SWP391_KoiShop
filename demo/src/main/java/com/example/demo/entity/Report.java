package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    String reportMessage;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    Account customer;

    @OneToOne
    @JoinColumn(name = "order_id")
    Orders orders;
}
