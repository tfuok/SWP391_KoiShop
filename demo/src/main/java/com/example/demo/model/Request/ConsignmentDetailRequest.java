package com.example.demo.model.Request;

import com.example.demo.entity.Koi;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ConsignmentDetailRequest {
    @NotNull(message = "Koi field cannot be null!")
    private KoiRequest koiRequest;
}