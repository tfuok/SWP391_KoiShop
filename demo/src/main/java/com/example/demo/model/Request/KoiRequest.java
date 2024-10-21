package com.example.demo.model.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KoiRequest {
    @NotBlank(message = "Name cannot be blank!")
    String name;

    @Min(value = 0, message = "Price must be positive")
    float price;

    String vendor;

    @NotBlank(message = "Gender must not be blank!")
    String gender;

    int bornYear;

    @Min(value = 0, message = "Size must be positive ")
    int size;

    List<Long> breedId;

    String origin;

    @Size(max = 500, message = "Description cannot be longer than 500 characters")
    String description;

    int quantity;

    String imageUrl;

    List<ImageListRequest> imagesList;

    float salePrice;
}
