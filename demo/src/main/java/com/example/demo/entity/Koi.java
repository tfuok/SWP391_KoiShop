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
    @NotBlank(message = "Name cannot be blank!")
    String name;

    @NotBlank(message = "Code must not be blank!")
    @Pattern(regexp = "K\\d{3}", message = "Code must be like (Kxxx)")
    @Column(unique = true)
    String koiCode;

    @Min(value = 0, message = "Price must be positive")
    float price;

    String vendor;
    @NotBlank(message = "Gender must not be blank!")
    String gender;

    int bornYear;

    @Min(value = 0, message = "Size must be positive ")
    int size;

    @NotBlank(message = "Breed must not be blank!")
    String breed;

    String origin;

    @Size(max = 500, message = "Description cannot be longer than 500 characters")
    String description;

    /**
     * If true, the koi has been sold.
     * If false, the koi is still available.
     */
    boolean sold;
    boolean isDeleted = false;
}
