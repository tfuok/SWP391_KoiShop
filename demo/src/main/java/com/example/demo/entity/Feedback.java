package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    private String content;

    private int rating;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    Account customer;
}
