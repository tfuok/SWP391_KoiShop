package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.ConsignmentDetailRequest;
import com.example.demo.model.Request.ConsignmentRequest;
import com.example.demo.repository.CareTypeRepository;
import com.example.demo.repository.ConsignmentRepository;
import com.example.demo.repository.KoiRepository;
import com.example.demo.util.DateUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ConsignmentService {

    @Autowired
    private ConsignmentRepository consignmentRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private KoiRepository koiRepository;

    @Autowired
    private CareTypeRepository careTypeRepository;

    /**
     * Creates a new Consignment based on the provided request.
     *
     * @param consignmentRequest The request containing consignment details.
     * @return The newly created Consignment entity.
     */
    public Consignment createConsignment(ConsignmentRequest consignmentRequest) {
        // Map from ConsignmentRequest to Consignment entity
        //Consignment consignment = modelMapper.map(consignmentRequest, Consignment.class);
        Consignment consignment = new Consignment();
        consignment.setType(consignmentRequest.getType());
        consignment.setAddress(consignmentRequest.getAddress());
        consignment.setDescription(consignmentRequest.getDescription());
        consignment.setStatus(Status.PENDING);

        // Set the account to the currently authenticated user
        Account account = authenticationService.getCurrentAccount();
        consignment.setAccount(account);

        // Set creation date
        consignment.setCreateDate(new Date());

        // Normalize and set startDate and endDate
        Date normalizedStartDate = DateUtils.normalizeDate(consignmentRequest.getStartDate());
        Date normalizedEndDate = DateUtils.normalizeDate(consignmentRequest.getEndDate());
        consignment.setStartDate(normalizedStartDate);
        consignment.setEndDate(normalizedEndDate);

        // Set CareType
        CareType careType = careTypeRepository.findByCareTypeId(consignmentRequest.getCareTypeId());
        if (careType == null) {
            throw new NotFoundException("CareType not found with ID: " + consignmentRequest.getCareTypeId());
        }
        consignment.setCareType(careType);

        // Calculate and set cost if type is "OFFLINE"
        if (consignment.getType() == Type.OFFLINE) {
            double estimateCost = calculateTotalCost(
                    careType.getCostPerDay(),
                    consignmentRequest.getConsignmentDetailRequests().size(),
                    normalizedStartDate,
                    normalizedEndDate
            );
            consignment.setCost(String.valueOf(estimateCost));
        }

        // Initialize ConsignmentDetails list
        List<ConsignmentDetails> consignmentDetailsList = new ArrayList<>();
        for (ConsignmentDetailRequest consignmentDetailRequest : consignmentRequest.getConsignmentDetailRequests()) {
            Koi koi = koiRepository.findKoiByIdAndIsDeletedFalse(consignmentDetailRequest.getId());
            if (koi == null) {
                throw new NotFoundException("Koi not found with ID: " + consignmentDetailRequest.getId());
            }

            ConsignmentDetails consignmentDetail = new ConsignmentDetails();
            consignmentDetail.setConsignment(consignment);
            consignmentDetail.setKoi(koi);

            consignmentDetailsList.add(consignmentDetail);
        }

        consignment.setConsignmentDetails(consignmentDetailsList);

        // Save consignment (cascade will save consignmentDetails)
        return consignmentRepository.save(consignment);
    }

    /**
     * Retrieves all active consignments.
     *
     * @return A list of active Consignments.
     */
    public List<Consignment> getAllConsignments() {
        return consignmentRepository.findByIsDeletedFalse();
    }

    /**
     * Deletes (soft deletes) a consignment by its ID.
     *
     * @param id The ID of the consignment to delete.
     * @return The updated Consignment entity.
     */
    public Consignment deleteConsignment(long id) {
        Consignment consignment = consignmentRepository.findConsignmentById(id);
        if (consignment == null) {
            throw new NotFoundException("Consignment not found with ID: " + id);
        }

        consignment.setIsDeleted(true);
        return consignmentRepository.save(consignment);
    }

    /**
     * Updates an existing Consignment based on the provided request.
     *
     * @param consignmentRequest The request containing updated consignment details.
     * @param id                 The ID of the consignment to update.
     * @return The updated Consignment entity.
     */
    public Consignment updateConsignment(ConsignmentRequest consignmentRequest, long id) {
        Consignment foundConsignment = consignmentRepository.findConsignmentById(id);
        if (foundConsignment == null) {
            throw new NotFoundException("Consignment not found with ID: " + id);
        }

        // Update basic fields
        foundConsignment.setType(consignmentRequest.getType());
        foundConsignment.setDescription(consignmentRequest.getDescription());

        // Normalize and set startDate and endDate
        Date normalizedStartDate = DateUtils.normalizeDate(consignmentRequest.getStartDate());
        Date normalizedEndDate = DateUtils.normalizeDate(consignmentRequest.getEndDate());
        foundConsignment.setStartDate(normalizedStartDate);
        foundConsignment.setEndDate(normalizedEndDate);

        // Update CareType
        CareType careType = careTypeRepository.findByCareTypeId(consignmentRequest.getCareTypeId());
        if (careType == null) {
            throw new NotFoundException("CareType not found with ID: " + consignmentRequest.getCareTypeId());
        }
        foundConsignment.setCareType(careType);

        // Recalculate cost if type is "OFFLINE"
        if (foundConsignment.getType() == Type.OFFLINE) {
            double estimateCost = calculateTotalCost(
                    careType.getCostPerDay(),
                    consignmentRequest.getConsignmentDetailRequests().size(),
                    normalizedStartDate,
                    normalizedEndDate
            );
            foundConsignment.setCost(String.valueOf(estimateCost));
        }

        // Update ConsignmentDetails
        List<ConsignmentDetails> updatedDetails = new ArrayList<>();
        for (ConsignmentDetailRequest detailRequest : consignmentRequest.getConsignmentDetailRequests()) {
            Koi koi = koiRepository.findKoiByIdAndIsDeletedFalse(detailRequest.getId());
            if (koi == null) {
                throw new NotFoundException("Koi not found with ID: " + detailRequest.getId());
            }

            ConsignmentDetails consignmentDetail = new ConsignmentDetails();
            consignmentDetail.setConsignment(foundConsignment);
            consignmentDetail.setKoi(koi);
            updatedDetails.add(consignmentDetail);
        }

        // Replace existing details with updated ones
        foundConsignment.getConsignmentDetails().clear();
        foundConsignment.getConsignmentDetails().addAll(updatedDetails);

        return consignmentRepository.save(foundConsignment);
    }

    /**
     * Retrieves consignments associated with a specific user (account) ID.
     *
     * @param accountId The ID of the account.
     * @return A list of Consignments linked to the account.
     */
    public List<Consignment> getConsignmentsByUserId(long accountId) {
        return consignmentRepository.findByAccount_IdAndIsDeletedFalse(accountId);
    }

    /**
     * Calculates the total cost of a consignment.
     *
     * @param costPerDay The cost per day for the care type.
     * @param quantity   The number of Koi involved.
     * @param startDate  The start date of the consignment.
     * @param endDate    The end date of the consignment.
     * @return The total estimated cost.
     */
    public static double calculateTotalCost(double costPerDay, int quantity, Date startDate, Date endDate) {
        // Calculate the difference in milliseconds
        long diffInMillies = endDate.getTime() - startDate.getTime();

        // Calculate the number of days
        long daysBetween = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

        // Return total cost
        return daysBetween * costPerDay * quantity;
    }
}
