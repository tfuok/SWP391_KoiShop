package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    Date date;

    double total;

    String description;

    @Enumerated(EnumType.STRING)
    Status status;


    double finalAmount;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    @JsonIgnore
    Account customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonIgnore
    List<OrderDetails> orderDetails;

    @OneToOne(mappedBy = "orders")
    @JsonIgnore
    Payment payment;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    @JsonIgnore
    Account staff;

    @OneToOne(mappedBy = "orders")
    @JsonIgnore
    Feedback feedback;
}
