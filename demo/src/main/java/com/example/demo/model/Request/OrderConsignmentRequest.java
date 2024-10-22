package com.example.demo.model.Request;

import com.example.demo.entity.CareType;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OrderConsignmentRequest {
    private List<OrderDetailRequest> detail;
    private String description;
    private Date endDate;
    private String voucherCode;
    private long careTypeId;
    private String image;

}
