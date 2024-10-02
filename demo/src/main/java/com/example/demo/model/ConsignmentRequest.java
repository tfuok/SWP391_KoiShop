package com.example.demo.model;

import jakarta.validation.constraints.NotBlank;

public class ConsignmentRequest {
    @NotBlank(message = "Type cannot be blank!")
    String type;
    @NotBlank(message = "Description cannot be blank!")
    String description;

    String createDate;

    Long koiID;
}
