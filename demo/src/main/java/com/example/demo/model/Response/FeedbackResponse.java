package com.example.demo.model.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FeedbackResponse {
    long id;
    String content;
    int rating;
    String username;
    long orderId;
}
