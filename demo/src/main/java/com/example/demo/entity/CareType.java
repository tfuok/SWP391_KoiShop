package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CareType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long careTypeId;

    String careTypeName;

    long costPerDay;

    boolean isDeleted;
}