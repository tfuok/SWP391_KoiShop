package com.example.demo.model.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailResponse {
    private long koiId;
    private double price;
    private String koiName;
    private String imageUrl;
}
