package com.example.demo.model.Response;

import com.example.demo.entity.Account;
import lombok.Data;

@Data
public class StaffResponse {
    Account staff;
    long orderCount;
    long consignmentCount;
}
