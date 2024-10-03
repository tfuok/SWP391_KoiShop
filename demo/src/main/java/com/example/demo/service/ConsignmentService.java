package com.example.demo.service;

import com.example.demo.entity.Account;
import com.example.demo.entity.CareType;
import com.example.demo.entity.Consignment;

import com.example.demo.entity.Koi;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.ConsignmentCustomerRequest;
import com.example.demo.model.Request.ConsignmentRequest;

import com.example.demo.repository.CareTypeRespority;
import com.example.demo.repository.ConsignmentRespority;
import com.example.demo.repository.KoiRepository;
import io.jsonwebtoken.Claims;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ConsignmentService {
    @Autowired
    ConsignmentRespority consignmentRespority;
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

    public Consignment createConsignment(ConsignmentCustomerRequest consignmentCustomerRequest) {
        try {
            //MAP TU CUSTOMER CONSIGNMENT THANH CONSIGNMENT
            Consignment consignment = modelMapper.map(consignmentCustomerRequest, Consignment.class);
            //DAT ACCOUNT CHO CONSIGNMENT = ACCOUNT CUA NGUOI DANG NHAP
            Account accountRequest = authenticationService.getCurrentAccount();
            consignment.setAccount(accountRequest);
            //DAT NGAY TAO CHO CONSIGNMENT
            consignment.setCreateDate(new Date());
            //DAT TRANG THAI CHO STATUS CONSIGNMENT MOI TAO = "Pending"
            consignment.setStatus("Pending");
            //DAT CARE TYPE
            CareType careType = careTypeRespority.findCareTypeByCareTypeId(consignmentCustomerRequest.getCareTypeId());
            if(careType == null) throw new NotFoundException("CareType not found");
            consignment.setCareType(careType);
            //LUU
            Consignment newConsignment = consignmentRespority.save(consignment);
            return newConsignment;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public List<Consignment> getAllConsignment() {
        List<Consignment> consignments = consignmentRespority.findConsignmentByIsDeletedFalse();
        return consignments;
    }
    public Consignment deleteConsignment(long id) {
        Consignment consignment = consignmentRespority.findConsignmentByconsignmentID(id);
        if (consignment == null) {
            throw new NotFoundException("Consignment not found!");
        }

        consignment.setIsDeleted(true);
        return consignmentRespority.save(consignment);
    }
    public Consignment updateConsignment(ConsignmentRequest consignmentRequest, long id) {

        Consignment foundConsignment = consignmentRespority.findConsignmentByconsignmentID(id);
        if (foundConsignment == null) {
            throw new NotFoundException("Koi not found");
        }

        foundConsignment.setType(consignmentRequest.getType());
        foundConsignment.setDescription(consignmentRequest.getDescription());
        CareType careType = careTypeRespority.findCareTypeByCareTypeId(consignmentRequest.getCareTypeId());
        if(careType == null) throw new NotFoundException("CareType not found");
        foundConsignment.setCareType(careType);
        return consignmentRespority.save(foundConsignment);
    }
    public List<Koi> getKoibyAccountId(long id) {
        Account currentAccount = (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return koiRepository.findByAccountId(currentAccount.getId());
    }

}
