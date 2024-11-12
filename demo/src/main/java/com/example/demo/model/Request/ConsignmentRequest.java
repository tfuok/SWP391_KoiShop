package com.example.demo.model.Request;

import com.example.demo.entity.Status;
import com.example.demo.entity.Type;
import com.example.demo.exception.ValidStartDate;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Data
public class ConsignmentRequest {

    private Type type;

    @NotBlank(message = "Address cannot be blank!")
    private String address;

    @Future(message = "Start Date must be in the future!")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @ValidStartDate
    private Date startDate;

    @Future(message = "End Date must be in the future!")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date endDate;

    @NotBlank(message = "Description cannot be blank!")
    private String description;

    private Long careTypeId;

    @NotEmpty(message = "ConsignmentDetail cannot be empty!")
    @Valid
    private List<ConsignmentDetailRequest> consignmentDetailRequests = new ArrayList<>();
}
