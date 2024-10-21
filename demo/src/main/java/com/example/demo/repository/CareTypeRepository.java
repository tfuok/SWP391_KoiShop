package com.example.demo.repository;

import com.example.demo.entity.CareType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CareTypeRepository extends JpaRepository<CareType, Long> {

    // Standard naming convention without repeating the entity name
    CareType findByCareTypeName(String careTypeName);

    CareType findByCareTypeId(long id);

    // Adjusted method name to match the interpreted property name
    List<CareType> findByDeletedFalse();

    List<CareType> findByCareTypeNameNot(String careTypeName);

}
