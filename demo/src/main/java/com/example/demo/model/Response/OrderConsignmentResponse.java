package com.example.demo.model.Response;

import com.example.demo.entity.Consignment;
import com.example.demo.entity.Orders;
import lombok.AllArgsConstructor;
import lombok.Data;
@AllArgsConstructor
@Data
public class OrderConsignmentResponse {
    private Orders order;
    private Consignment consignment;
}
