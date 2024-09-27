package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.NumberFormat;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Koi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    String name;
    float price;

    String vendor;

    String gender;

    int bornYear;

    int size;

    String breed;

    String origin;

    String description;

    /**
     * If true, the koi has been sold.
     * If false, the koi is still available.
     */
    boolean sold = false;

    boolean isDeleted = false;

    @ManyToOne
    @JoinColumn(name = "account_id")
    Account account;
}
