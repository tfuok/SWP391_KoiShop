package com.example.demo.model.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConsignmentDetailResponse {
    private long koiId;
    private double price;
    private String koiName;
    private String imageUrl;
}
