package com.example.demo.model.Request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleRequest {
    private long koiId;
    @Min(value = 0, message = "Phần trăm giảm giá phải lớn hơn hoặc bằng 0!")
    @Max(value = 100, message = "Phần trăm giảm giá không được vượt quá 100!")
    private float salePercentage;
    private LocalDateTime saleStartTime;
    private LocalDateTime saleEndTime;
}
