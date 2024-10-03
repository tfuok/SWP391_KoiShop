package com.example.demo.model.Response;

import com.example.demo.entity.Breed;
import lombok.Data;

@Data
public class KoiResponse {
    private Long id;
    private String name;
    private float price;
    private String vendor;
    private String gender;
    private int bornYear;
    private int size;
    private String origin;
    private String description;
    private boolean sold;
    private boolean deleted;

    private Breed breed;  // Thêm thuộc tính Breed vào trong phản hồi
}
