package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
public class ConsignmentDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "consignment_id")
    private Consignment consignment;

    // Owning side of the relationship
    @OneToOne
    @JoinColumn(name = "koi_id", unique = true)
    @JsonBackReference("consignmentDetails")
    private Koi koi;
}
