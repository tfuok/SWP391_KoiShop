package com.example.demo.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Koi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;
    private float price;
    private float salePrice;
    private float salePercentage;
    private String vendor;
    private String gender;
    private int bornYear;
    private int size;
    private String origin;
    private String description;
    private String images;
    private boolean sold = false;
    private boolean isConsignment = false;
    private boolean isDeleted = false;
    private int quantity;


    // Many-to-Many with Breed
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "koi_breed",  // This is the join table name
            joinColumns = @JoinColumn(name = "koi_id"),
            inverseJoinColumns = @JoinColumn(name = "breed_id")
    )
    private Set<Breed> breeds = new HashSet<>();

    // Correct ManyToOne mapping with Account
    @ManyToOne
    @JoinColumn(name = "account_id")  // No need for JoinTable here
    private Account account;

    @OneToMany(mappedBy = "koi")
    @JsonIgnore
    Set<OrderDetails> orderDetails;

    @OneToMany(mappedBy = "koi", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Images> imagesList = new ArrayList<>() ; // Initialize the list to avoid null references

    // One-to-One with Certificate
    @OneToOne(mappedBy = "koi", cascade = CascadeType.ALL, orphanRemoval = true)
    private Certificate certificate;

    // One-to-Many with ConsignmentDetails
    @OneToMany(mappedBy = "koi", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<ConsignmentDetails> consignmentDetails;
}
