package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
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

    private String vendor;

    private String gender;

    private int bornYear;

    private int size;

    private String origin;

    private String description;

    private String images;

    private boolean sold = false;

    private boolean isDeleted = false;

    private int quantity;

    // Correct ManyToMany mapping with Breed
    @ManyToMany(fetch = FetchType.EAGER)
    @JsonIgnore
    @JoinTable(
            name = "koi_breed",  // This is the join table name
            joinColumns = @JoinColumn(name = "koi_id"),
            inverseJoinColumns = @JoinColumn(name = "breed_id")
    )
    private Set<Breed> breeds;

    // Correct ManyToOne mapping with Account
    @ManyToOne
    @JoinColumn(name = "account_id")  // No need for JoinTable here
    private Account account;

    @OneToMany(mappedBy = "koi")
    @JsonIgnore
    Set<OrderDetails> orderDetails;

    @OneToMany(mappedBy = "koi", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Images> imagesList ; // Initialize the list to avoid null references


//    boolean isLot;
}
