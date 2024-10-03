package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.NumberFormat;

import java.util.List;

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

    String origin;

    String description;

    /**
     * If true, the koi has been sold.
     * If false, the koi is still available.
     */
    boolean sold = false;

    boolean isDeleted = false;


    @ManyToOne
    @JoinColumn(name = "breed_id")
    @JsonBackReference  // Ngăn vòng lặp tuần hoàn khi chuyển sang JSON
    Breed breed;

    @ManyToOne
    @JoinColumn(name = "account_id")
    Account account;

    @ManyToOne
    @JoinColumn(name = "consignment_id")
    Consignment consignment;
    @OneToMany(mappedBy = "koi", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Image> images;
}
