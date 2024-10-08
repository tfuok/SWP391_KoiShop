package com.example.demo.model.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CareTypeRequest {
    @NotBlank(message ="Care Type Name must not be blank")
    String careTypeName;
    @NotBlank(message ="Cost must not be blank")
    double costPerDay;
}
