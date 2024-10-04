package com.example.demo.repository;

import com.example.demo.entity.Breed;
import com.example.demo.entity.Koi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KoiRepository extends JpaRepository<Koi, Long> {

    Koi findKoiById(long id);

    List<Koi> findKoiByIsDeletedFalse();

    Koi findKoiByName(String name);

    List<Koi> findByBreed(Breed breed);

    Page<Koi> findAllByIsDeletedFalse(Pageable pageable);
}
