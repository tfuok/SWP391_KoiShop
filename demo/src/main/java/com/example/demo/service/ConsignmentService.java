package com.example.demo.service;

import com.example.demo.entity.Account;
import com.example.demo.entity.Consignment;
import com.example.demo.entity.Koi;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.ConsignmentRequest;
import com.example.demo.model.KoiRequest;
import com.example.demo.repository.ConsignmentRespority;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ConsignmentService {
    @Autowired
    ConsignmentRespority consignmentRespority;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    AuthenticationService authenticationService;

    public Consignment createConsignment(ConsignmentRequest consignmentRequest) {
        try {
            Consignment consignment = modelMapper.map(consignmentRequest, Consignment.class);
            Account accountRequest = authenticationService.getCurrentAccount();
            consignment.setAccount(accountRequest);
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
        return consignmentRespority.save(foundConsignment);
    }
}
