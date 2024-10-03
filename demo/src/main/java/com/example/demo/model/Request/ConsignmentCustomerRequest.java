package com.example.demo.model.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConsignmentCustomerRequest {
    @NotBlank(message = "Type cannot be blank!")
    String type;
    @NotBlank(message = "Address cannot be blank!")
    String address;
    @NotBlank(message = "Cost cannot be blank!")
    String cost;
    @NotBlank(message = "Description cannot be blank!")
    String description;
    @NotBlank(message = "Care Type cannot be blank!")
    Long careTypeId;
}
