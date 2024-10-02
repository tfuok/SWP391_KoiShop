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

import java.util.List;

@Service
public class ConsignmentService {
    @Autowired
    ConsignmentRespority consignmentRespority;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    AuthenticationService authenticationService;

    public ConsignmentService createConsignment(ConsignmentRequest consignmentRequest, long Id) {
        try {
            Consignment consignment = modelMapper.map(consignmentRequest, Consignment.class);
            Account accountRequest = authenticationService.getCurrentAccount();
            consignment.setAccount(accountRequest);
            consignment.setKoiID(Id);
            Consignment newConsignment = consignmentRespority.save(consignment);

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
        Consignment consignment = consignmentRespority.findConsignmentById(id);
        if (consignment == null) {
            throw new NotFoundException("Consignment not found!");
        }
        consignment.setIsDeleted(true);
        return consignmentRespority.save(consignment);
    }
    public Consignment updateConsignment(ConsignmentRequest consignmentRequest, long id) {
        //b1: tìm tới student có id như FE cung cấp
        Koi foundKoi = consignmentRespority.findConsignmentById(id);
        if (foundKoi == null) {
            throw new NotFoundException("Koi not found");
        }
        //=> tồn tại
        foundKoi.setName(koiRequest.getName());
        foundKoi.setPrice(koiRequest.getPrice());
        foundKoi.setVendor(koiRequest.getVendor());
        foundKoi.setGender(koiRequest.getGender());
        foundKoi.setBornYear(koiRequest.getBornYear());
        foundKoi.setSize(koiRequest.getSize());
        foundKoi.setBreed(koiRequest.getBreed());
        foundKoi.setOrigin(koiRequest.getOrigin());
        foundKoi.setDescription(koiRequest.getDescription());
        return koiRepository.save(foundKoi);
    }
}
