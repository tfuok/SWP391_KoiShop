package com.example.demo.model.Response;

import lombok.Data;

import java.util.Date;

@Data
public class KoiOfflineConsignmentResponse {
    long id;
    String imgUrl;
    Date endDate;
    String isConsignment;
    float price;
}
