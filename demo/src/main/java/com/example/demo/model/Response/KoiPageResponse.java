package com.example.demo.model.Response;

import lombok.Data;

import java.util.List;

@Data
public class KoiPageResponse {
    private List<KoiResponse> content;  // List of KoiResponse objects
    private int pageNumber;
    private long totalElements;
    private int totalPages;
}
