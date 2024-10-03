package com.example.demo.model.Response;

import lombok.Data;

import java.util.Date;

@Data
public class ConsignmentResponse{
    private long consignmentID;
    private String type;
    private String address;
    private String description;
    private String cost;
    private Date startDate;
    private Date endDate;
    private Date createDate;
    private String status;
    private String careTypeName; // only careTypeName here


}
