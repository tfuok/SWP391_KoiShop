package com.example.demo.model.Response;

import lombok.Data;

import java.util.List;

@Data
public class KoiLotPageResponse {
    List<KoiLotResponse> content;
    int pageNumber;
    long totalElements;
    int totalPages;
}
