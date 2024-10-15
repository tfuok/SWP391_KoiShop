package com.example.demo.entity;

import jakarta.persistence.*;
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

    double costPerDay;

    boolean deleted;

}