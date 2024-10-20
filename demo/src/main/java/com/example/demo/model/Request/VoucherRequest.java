package com.example.demo.model.Request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class VoucherRequest {

    String name;

    String code;

    @Min(value = 0, message = "Discount value must be greater than or equal to 0")
    double discountValue;
    @NotNull(message = "Expired date cannot be null")
    @Future(message = "Expired date must be in the future")
    Date expiredDate;
    @Min(value = 0, message = "Quantity must be greater than or equal to 0")
    int quantity;
}
