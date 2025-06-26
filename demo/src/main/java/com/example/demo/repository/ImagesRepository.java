package com.example.demo.repository;

import com.example.demo.entity.Images;
import com.example.demo.entity.Koi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImagesRepository extends JpaRepository<Images, Long> {
    List<Images> findImagesByKoi(Koi koi);
}
