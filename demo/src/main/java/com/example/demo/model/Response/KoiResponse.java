package com.example.demo.model.Response;

import com.example.demo.entity.Certificate;
import com.example.demo.entity.Images;
import com.example.demo.model.Request.ImageListRequest;
import lombok.Data;

import java.util.List;

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
    private boolean consignment;
    private Long accountId;
    private List<String> breeds;
    private String images;
    private int quantity;
    private boolean isConsignment;
    private Certificate certificate;
    private List<String> imagesList;
}
