package com.example.demo.model.Request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;

@Data
public class VoucherRequest {
    @Min(value = 0, message = "Discount value must be greater than or equal to 0")
    float discountValue;
    @NotNull(message = "Expired date cannot be null")
    @Future(message = "Expired date must be in the future")
    Date expiredDate;
    @Min(value = 0, message = "Quantity must be greater than or equal to 0")
    int quantity;
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    String description;
    @Min(value = 0, message = "Minimum points must be greater than or equal to 0")
    int minimumPoints; // diem tich luy toi thieu de co the su dung dc voucher
    @Min(value = 0, message = "Minimum price must be greater than or equal to 0")
    float minimumPrice; //giá đơn hàng tối thieu de co the su dung dc voucher
}
