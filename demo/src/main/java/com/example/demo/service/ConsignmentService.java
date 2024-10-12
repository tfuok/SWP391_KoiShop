package com.example.demo.service;

import com.example.demo.entity.*;

import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.ConsignmentDetailRequest;
import com.example.demo.model.Request.ConsignmentRequest;

import com.example.demo.repository.CareTypeRespority;
import com.example.demo.repository.ConsignmentRepository;
import com.example.demo.repository.KoiRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ConsignmentService {
    @Autowired
    ConsignmentRepository consignmentRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    KoiService koiService;
    @Autowired
    TokenService tokenService;
    @Autowired
    KoiRepository koiRepository;
    @Autowired
    CareTypeService careTypeService;
    @Autowired
    private CareTypeRespority careTypeRespority;

    public Consignment createConsignment(ConsignmentRequest consignmentRequest) {
        try {
            //MAP TU CUSTOMER CONSIGNMENT THANH CONSIGNMENT
            Consignment consignment = modelMapper.map(consignmentRequest, Consignment.class);
            //DAT ACCOUNT CHO CONSIGNMENT = ACCOUNT CUA NGUOI DANG NHAP
            Account accountRequest = authenticationService.getCurrentAccount();
            consignment.setAccount(accountRequest);
            //DAT NGAY TAO CHO CONSIGNMENT
            consignment.setCreateDate(new Date());
            //DAT TRANG THAI CHO STATUS CONSIGNMENT MOI TAO = "Pending"
            consignment.setStatus("Pending");
            //DAT CARE TYPE
            CareType careType = careTypeRespority.findCareTypeByCareTypeId(consignmentRequest.getCareTypeId());
            //
            if(consignmentRequest.getType()=="Offline"){
                double estimateCost = calculateTotalCost(careType.getCostPerDay(), consignmentRequest.getQuantity(), consignmentRequest.getStartDate(),consignmentRequest.getEndDate());
                String estimatedCostS = String.valueOf(estimateCost);
                consignment.setCost(estimatedCostS);
            }
            if(careType == null) throw new NotFoundException("CareType not found");
            consignment.setCareType(careType);

            List<ConsignmentDetails> consignmentDetails = new ArrayList<>();
            for(ConsignmentDetailRequest consignmentDetailRequest : consignmentRequest.getConsignmentDetailRequests()){
                koiRepository.findKoiByIdAndIsDeletedFalse(consignmentDetailRequest.getId());
            ConsignmentDetails consignmentDetail = new ConsignmentDetails();

            }
            //LUU
            Consignment newConsignment = consignmentRepository.save(consignment);
            return newConsignment;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public List<Consignment> getAllConsignment() {
        List<Consignment> consignments = consignmentRepository.findConsignmentByIsDeletedFalse();
        return consignments;
    }
    public Consignment deleteConsignment(long id) {
        Consignment consignment = consignmentRepository.findConsignmentByconsignmentID(id);
        if (consignment == null) {
            throw new NotFoundException("Consignment not found!");
        }

        consignment.setIsDeleted(true);
        return consignmentRepository.save(consignment);
    }
    public Consignment updateConsignment(ConsignmentRequest consignmentRequest, long id) {

        Consignment foundConsignment = consignmentRepository.findConsignmentByconsignmentID(id);
        if (foundConsignment == null) {
            throw new NotFoundException("Consignment not found!");
        }

        foundConsignment.setType(consignmentRequest.getType());
        foundConsignment.setDescription(consignmentRequest.getDescription());
        CareType careType = careTypeRespority.findCareTypeByCareTypeId(consignmentRequest.getCareTypeId());
        if(careType == null) throw new NotFoundException("CareType not found");
        foundConsignment.setCareType(careType);
        return consignmentRepository.save(foundConsignment);
    }
    public List<Koi> getKoibyAccountId(long id) {
        Account currentAccount = (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return koiRepository.findByAccountId(currentAccount.getId());
    }

    public static double calculateTotalCost(double costPerDay,int quantity, Date startDate, Date endDate) {
        // Tính số milliseconds giữa startDate và endDate
        long diffInMillies = endDate.getTime() - startDate.getTime();

        // Tính số ngày
        long daysBetween = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

        // Trả về tổng chi phí
        return daysBetween * costPerDay * quantity;
    }
}
