package com.example.demo.model.Response;

import com.example.demo.entity.Breed;
import lombok.Data;

import java.util.List;

@Data
public class KoiLotResponse {
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
    private List<String> breeds;
    private String imageUrl;
}
