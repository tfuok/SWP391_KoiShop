package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Breed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private boolean isDeleted = false;

    private String imageUrl;

    // A breed can have many koi
    @OneToMany(mappedBy = "breed", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference  // Manages references and prevents infinite loops in serialization
    private List<Koi> kois;

    // Correct ManyToMany mapping with KoiLot
    @ManyToMany
    @JoinTable(
            name = "koi_lot_breed",  // Should match the JoinTable in KoiLot
            joinColumns = @JoinColumn(name = "breed_id"),
            inverseJoinColumns = @JoinColumn(name = "koi_lot_id")
    )
    @JsonIgnore
    private List<KoiLot> koiLots;
}
