package com.example.demo.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConsignmentRequest {
    @NotBlank(message = "Type cannot be blank!")
    String type;
    @NotBlank(message = "Description cannot be blank!")
    String description;
    @NotBlank(message = "Status cannot be blank!")
    String status;
}
