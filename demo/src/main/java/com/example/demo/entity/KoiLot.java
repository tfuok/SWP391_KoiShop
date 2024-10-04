package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class KoiLot {

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

    /**
     * If true, the koi has been sold.
     * If false, the koi is still available.
     */
    private boolean sold = false;

    private boolean isDeleted = false;

    private int quantity;

    // Correct ManyToMany mapping with Breed
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "koi_lot_breed",
            joinColumns = @JoinColumn(name = "koi_lot_id"),
            inverseJoinColumns = @JoinColumn(name = "breed_id")
    )
    @JsonIgnore
    private List<Breed> breeds;

    // Correct ManyToOne mapping with Account
    @ManyToOne
    @JoinColumn(name = "account_id")  // No need for JoinTable here
    private Account account;
}
