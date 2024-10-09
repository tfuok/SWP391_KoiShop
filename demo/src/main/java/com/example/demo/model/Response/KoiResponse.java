package com.example.demo.model.Response;

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
    private Long accountId;
    private String breedName;
    private String imageUrl;
}
