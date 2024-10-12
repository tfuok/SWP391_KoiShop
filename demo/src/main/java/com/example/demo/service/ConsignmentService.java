package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.ConsignmentDetailRequest;
import com.example.demo.model.Request.ConsignmentRequest;
import com.example.demo.repository.CareTypeRepository; // Corrected spelling
import com.example.demo.repository.ConsignmentRepository;
import com.example.demo.repository.KoiRepository;
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
    private KoiService koiService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private KoiRepository koiRepository;

    @Autowired
    private CareTypeService careTypeService;

    @Autowired
    private CareTypeRepository careTypeRepository; // Corrected spelling

    public Consignment createConsignment(ConsignmentRequest consignmentRequest) {
        try {
            // Map from ConsignmentRequest to Consignment entity
            Consignment consignment = modelMapper.map(consignmentRequest, Consignment.class);

            // Set the account to the currently authenticated user
            Account accountRequest = authenticationService.getCurrentAccount();
            consignment.setAccount(accountRequest);

            // Set creation date
            consignment.setCreateDate(new Date());

            // Set status to "Pending"
            consignment.setStatus("Pending");

            // Set CareType
            CareType careType = careTypeRepository.findCareTypeByCareTypeId(consignmentRequest.getCareTypeId());
            if (careType == null) {
                throw new NotFoundException("CareType not found");
            }
            consignment.setCareType(careType);

            // Calculate and set cost if type is "Offline"
            if ("Offline".equalsIgnoreCase(consignmentRequest.getType())) {
                double estimateCost = calculateTotalCost(
                        careType.getCostPerDay(),
                        consignmentRequest.getQuantity(),
                        consignmentRequest.getStartDate(),
                        consignmentRequest.getEndDate()
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
            Consignment newConsignment = consignmentRepository.save(consignment);
            return newConsignment;
        } catch (Exception e) {
            e.printStackTrace();
            throw e; // It's better to rethrow or handle appropriately
        }
    }

    public List<Consignment> getAllConsignment() {
        return consignmentRepository.findByIsDeletedFalse();
    }

    public Consignment deleteConsignment(long id) {
        Consignment consignment = consignmentRepository.findConsignmentById(id);
        if (consignment == null) {
            throw new NotFoundException("Consignment not found!");
        }

        consignment.setIsDeleted(true);
        return consignmentRepository.save(consignment);
    }

    public Consignment updateConsignment(ConsignmentRequest consignmentRequest, long id) {
        Consignment foundConsignment = consignmentRepository.findConsignmentById(id);
        if (foundConsignment == null) {
            throw new NotFoundException("Consignment not found!");
        }

        foundConsignment.setType(consignmentRequest.getType());
        foundConsignment.setDescription(consignmentRequest.getDescription());

        CareType careType = careTypeRepository.findCareTypeByCareTypeId(consignmentRequest.getCareTypeId());
        if (careType == null) {
            throw new NotFoundException("CareType not found");
        }
        foundConsignment.setCareType(careType);

        // Optionally update cost if type is "Offline"
        if ("Offline".equalsIgnoreCase(consignmentRequest.getType())) {
            double estimateCost = calculateTotalCost(
                    careType.getCostPerDay(),
                    consignmentRequest.getQuantity(),
                    consignmentRequest.getStartDate(),
                    consignmentRequest.getEndDate()
            );
            foundConsignment.setCost(String.valueOf(estimateCost));
        }

        return consignmentRepository.save(foundConsignment);
    }

    public static double calculateTotalCost(double costPerDay, int quantity, Date startDate, Date endDate) {
        // Calculate the difference in milliseconds
        long diffInMillies = endDate.getTime() - startDate.getTime();

        // Calculate the number of days
        long daysBetween = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

        // Return total cost
        return daysBetween * costPerDay * quantity;
    }
}
