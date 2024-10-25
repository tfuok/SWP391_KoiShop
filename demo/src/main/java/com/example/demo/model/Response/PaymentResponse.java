package com.example.demo.model.Response;

import com.example.demo.entity.PaymentEnums;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private long id;
    private Date createAt;
    private double total;
    private PaymentEnums method;
    private OrderResponse order;
    private ConsignmentResponse consignment;
}
