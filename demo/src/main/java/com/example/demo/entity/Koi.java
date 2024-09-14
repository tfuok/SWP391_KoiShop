package com.example.demo.entity;

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
public class Koi {
    @NotBlank(message = "Name cannot be blank!")
    String name;

    @NotBlank(message = "Code must not be blank!")
    @Pattern(regexp = "K\\d{3}", message = "ID must be like (Kxxx)")
    long koiID;

    @Min(value = 0, message = "Price must be positive")
    float price;

    String vendor;
    @NotBlank(message = "Gender must not be blank!")
    String gender;

    @NotBlank(message = "Born year must not be blank!")
    @NumberFormat
    int bornYear;

    @NotBlank(message = "Born year must not be blank!")
    @Min(value = 0, message = "Size must be positive ")
    int size;

    @NotBlank(message = "Breed must not be blank!")
    String breed;

    @NotBlank(message = "Origin must not be blank!")
    String origin;

    @Size(max = 500, message = "Description cannot be longer than 500 characters")
    String description;

    /**
     * If true, the koi has been sold.
     * If false, the koi is still available.
     */
    boolean sold;
}
