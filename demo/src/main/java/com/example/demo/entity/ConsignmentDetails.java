package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
     long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "consignment_id")
     Consignment consignment;

    // Owning side of the relationship
    @ManyToOne
    @JoinColumn(name = "koi_id")
     Koi koi;
}
