package com.example.demo.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    // Many-to-Many with Breed
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "koi_breed",
            joinColumns = @JoinColumn(name = "koi_id"),
            inverseJoinColumns = @JoinColumn(name = "breed_id")
    )
    private Set<Breed> breeds;

    // Many-to-One with Account
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    // One-to-Many with OrderDetails
    @OneToMany(mappedBy = "koi")
    @JsonIgnore
    private Set<OrderDetails> orderDetails;

    // One-to-Many with Images
    @OneToMany(mappedBy = "koi", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Images> imagesList = new ArrayList<>(); // Initialized to avoid null references

    // One-to-One with Certificate
    @OneToOne(mappedBy = "koi", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("certificate")
    private Certificate certificate;

    // One-to-One with ConsignmentDetails
    @OneToOne(mappedBy = "koi", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("consignmentDetails")
    private ConsignmentDetails consignmentDetails;
}
