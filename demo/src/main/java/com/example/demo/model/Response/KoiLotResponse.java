package com.example.demo.model.Response;

import com.example.demo.model.Request.KoiLotRequest;
import com.example.demo.model.Request.KoiRequest;
import lombok.Data;

import java.util.List;

@Data
public class KoiLotResponse {
    List<KoiLotRequest> content;
    int pageNumber;
    long totalElements;
    int totalPages;
}
