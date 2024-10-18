package com.example.demo.model.Request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FeedbackRequest {
    private String content;

    private int rating;

}
