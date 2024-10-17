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

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Data
public class ConsignmentRequest {

    /**
     * The type of consignment.
     */

    private Type type;

    /**
     * The address for the consignment.
     */
    @NotBlank(message = "Address cannot be blank!")
    private String address;

    /**
     * The start date of the consignment.
     * Must be in the future and at least 7 days from the current date.
     */
    @Future(message = "Start Date must be in the future!")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date startDate;

    /**
     * The end date of the consignment.
     * Must be at least 1 day after the start date.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date endDate;

    /**
     * Custom validation to ensure endDate is at least 1 day after startDate.
     */
    @AssertTrue(message = "End Date must be at least 1 day after Start Date!")
    public boolean isValidEndDate() {
        if (endDate == null || startDate == null) {
            return false;
        }
        // Check if endDate is after startDate
        boolean isAfter = endDate.after(startDate);
        // Check if the difference is at least 1 day (in milliseconds)
        boolean isAtLeastOneDay = (endDate.getTime() - startDate.getTime()) >= TimeUnit.DAYS.toMillis(1);
        return isAfter && isAtLeastOneDay;
    }

    /**
     * Custom validation to ensure startDate is at least 7 days in the future.
     */
    @AssertTrue(message = "Start Date must be at least 7 days in the future!")
    public boolean isStartDateValid() {
        if (startDate == null) {
            return false;
        }
        Date currentDate = new Date();
        // Calculate the difference in milliseconds
        long diffInMillis = startDate.getTime() - currentDate.getTime();
        // Check if the difference is at least 7 days
        return diffInMillis >= TimeUnit.DAYS.toMillis(7);
    }

    /**
     * Description of the consignment.
     */
    @NotBlank(message = "Description cannot be blank!")
    private String description;

    /**
     * The status of the consignment.
     */


    /**
     * The ID of the care type associated with the consignment.
     */
    private Long careTypeId;

    /**
     * List of consignment detail requests.
     * Each detail request must be valid.
     */
    @NotEmpty(message = "ConsignmentDetail cannot be empty!")
    @Valid
    private List<ConsignmentDetailRequest> consignmentDetailRequests;
}
