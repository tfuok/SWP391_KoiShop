package com.example.demo.repository;

import com.example.demo.entity.CareType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareTypeRepository extends JpaRepository<CareType, Long> {
    public CareType findCareTypeByCareTypeName(String careTypeName);
    public CareType findCareTypeByCareTypeId(long id);}
