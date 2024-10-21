package com.example.demo.model.Response;

import lombok.Data;

@Data
public class ConsignmentDetailResponse {
    private long koiId;
    private double price;
    private String koiName;
    private String imageUrl;
}
