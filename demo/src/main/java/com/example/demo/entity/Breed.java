package com.example.demo.entity;

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
    long id;

    String name;

    boolean isDeleted = false;

    // Một giống cá có thể chứa nhiều cá koi
    @OneToMany(mappedBy = "breed", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference  // Quản lý tham chiếu và ngăn vòng lặp tuần hoàn
    private List<Koi> kois;

    // Nhiều giống cá thuộc về một lô cá
    @ManyToOne
    @JoinColumn(name = "koi_lot_id")  // Khóa ngoại liên kết đến bảng KoiLot
    private KoiLot koiLot;  // Tham chiếu ngược về lô cá
}
