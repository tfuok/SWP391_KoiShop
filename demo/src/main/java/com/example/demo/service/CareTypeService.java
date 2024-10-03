package com.example.demo.service;

import com.example.demo.entity.Breed;
import com.example.demo.entity.CareType;
import com.example.demo.entity.Koi;
import com.example.demo.exception.DuplicatedEntity;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Request.CareTypeRequest;
import com.example.demo.repository.CareTypeRespority;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CareTypeService {
    @Autowired
    CareTypeRespority careTypeRespority;
    @Autowired
    ModelMapper modelMapper;

    public CareType addNewCareType(CareTypeRequest careTypeRequest) {
        try {
            CareType newCareType = careTypeRespority.findCareTypeByCareTypeName(careTypeRequest.getCareTypeName());
            if (newCareType != null) throw new DuplicatedEntity("CareType existed!");
            CareType careType = modelMapper.map(careTypeRequest, CareType.class);
            return careTypeRespority.save(careType);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    public CareType updateCareType(CareTypeRequest careTypeRequest, long id){

        CareType foundCareType = careTypeRespority.findCareTypeByCareTypeId(id);
        if (foundCareType == null) {
            throw new NotFoundException("CareType not found");
        }
        //=> tồn tại
       foundCareType.setCareTypeName(careTypeRequest.getCareTypeName());
        foundCareType.setCostPerDay(careTypeRequest.getCostPerDay());
        return careTypeRespority.save(foundCareType);
        }
        public void deleteCareType(long id) {
            CareType foundCareType = careTypeRespority.findCareTypeByCareTypeId(id);
            if (foundCareType == null) {
                throw new NotFoundException("CareType not found");
            }
            foundCareType.setDeleted(true);
            careTypeRespority.save(foundCareType);
        }
        public List<CareType> getAllCareType() {
        List<CareType> CareTypes = careTypeRespority.findAll();
        return CareTypes;
    }
}
