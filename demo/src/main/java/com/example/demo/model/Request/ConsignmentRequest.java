package com.example.demo.model.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class ConsignmentRequest {
    @NotBlank(message = "Type cannot be blank!")
    String type;
    @NotBlank(message = "Address cannot be blank!")
    String address;
    @NotBlank(message = "Cost cannot be blank!")
    String cost;
    @NotBlank(message = "Start Date cannot be blank!")
    Date startDate;
    @NotBlank(message = "End Date cannot be blank!")
    Date endDate;
    @NotBlank(message = "Description cannot be blank!")
    String description;
    @NotBlank
    String status;
    @NotNull(message = "CareTypeId cannot be null")
    long careTypeId;

}
