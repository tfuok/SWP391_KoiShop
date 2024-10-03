package com.example.demo.model.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "CareTypeId cannot be null")
    long careTypeId;
}
