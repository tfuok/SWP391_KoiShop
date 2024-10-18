package com.example.demo.model.Response;

import lombok.Data;

import java.util.List;

@Data
public class KoiPageResponse {
    List<KoiResponse> content;
    int pageNumber;
    long totalElements;
    int totalPages;
}
