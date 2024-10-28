package com.example.demo.model.Request;

import com.example.demo.entity.Status;
import com.example.demo.entity.Type;
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
    private Date startDate;


    @Future(message = "End Date must be in the future!")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date endDate;


    @AssertTrue(message = "End Date must be at least 1 day after Start Date!")
    public boolean isValidEndDate() {
        if (endDate == null || startDate == null) {
            return false;
        }
        boolean isAfter = endDate.after(startDate);
        boolean isAtLeastOneDay = (endDate.getTime() - startDate.getTime()) >= TimeUnit.DAYS.toMillis(1);
        return isAfter && isAtLeastOneDay;
    }
    @AssertTrue(message = "Start Date must be at least 7 days in the future!")
    public boolean isStartDateValid() {
        if (startDate == null) {
            return false;
        }
        Date currentDate = new Date();
        long diffInMillis = startDate.getTime() - currentDate.getTime();
        return diffInMillis >= TimeUnit.DAYS.toMillis(7);
    }

    @NotBlank(message = "Description cannot be blank!")
    private String description;

    private Long careTypeId;

    @NotEmpty(message = "ConsignmentDetail cannot be empty!")
    @Valid
    private List<ConsignmentDetailRequest> consignmentDetailRequests = new ArrayList<>();
}
