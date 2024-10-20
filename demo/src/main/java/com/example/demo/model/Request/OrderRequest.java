package com.example.demo.model.Request;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OrderRequest {
    private List<OrderDetailRequest> detail;
    private String description;
    private Date endDate;
    private String voucherCode;
    private String image;
}
