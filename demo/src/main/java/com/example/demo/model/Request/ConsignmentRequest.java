package com.example.demo.model.Request;

import com.example.demo.entity.Koi;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ConsignmentRequest {
    @NotBlank(message = "Type cannot be blank!")
    String type;
    @NotBlank(message = "Address cannot be blank!")
    String address;
    @NotBlank(message = "Cost cannot be blank!")
    String cost;
    @NotBlank(message = "Quantity cannot be blank!")
    int quantity;
    @NotNull(message = "Start Date cannot be null!")
    @Future(message = "Start Date must be in the future!")
    private Date startDate;

    @NotNull(message = "End Date cannot be null!")
    private Date endDate;

    @AssertTrue(message = "End Date must be at least 1 day after Start Date!")
    public boolean isValidEndDate() {
        return endDate != null && startDate != null && endDate.after(startDate) &&
                (endDate.getTime() - startDate.getTime()) >= (24 * 60 * 60 * 1000);
    }

    @AssertTrue(message = "Start Date must be at least 7 days in the future!")
    public boolean isStartDateValid() {
        Date currentDate = new Date();
        return startDate != null && startDate.after(currentDate) &&
                (startDate.getTime() - currentDate.getTime()) >= (7 * 24 * 60 * 60 * 1000);
    }

    @NotBlank(message = "Description cannot be blank!")
    String description;
    @NotBlank
    String status;
    @NotNull(message = "CareTypeId cannot be null")
    long careTypeId;
    @NotNull(message = "Koi IDs cannot be null!")
    List<Long> koiIds; // Change to List of Koi IDs
}
