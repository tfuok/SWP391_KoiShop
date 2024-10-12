package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class OrderDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    double price;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore
    Orders order;

    @ManyToOne
    @JoinColumn(name = "koi_id")
    @JsonIgnore
    Koi koi;
}
