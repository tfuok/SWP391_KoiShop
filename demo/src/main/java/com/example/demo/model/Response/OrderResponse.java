package com.example.demo.model.Response;

import com.example.demo.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private long id;
    private Date date;
    private double total;
    private int rating;
    private String description;
    private Status status;
    private String feedback;
    private double finalAmount;
    private Long staffId;
    private Long customerId;
    private List<OrderDetailResponse> orderDetails;

}
