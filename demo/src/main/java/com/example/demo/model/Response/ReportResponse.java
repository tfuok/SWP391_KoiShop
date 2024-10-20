package com.example.demo.model.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponse {
    private long reportId;
    private String reportMessage;
    private String customerUsername;
    private long orderId;
}
