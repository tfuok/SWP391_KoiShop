package com.example.demo.repository;

import com.example.demo.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Voucher findVoucherById(long id);

    List<Voucher> findVoucherByIsDeletedFalse();
    @Query("SELECT v FROM Voucher v WHERE v.isDeleted = false AND v.expiredDate > :currentDate")
    List<Voucher> findActiveVouchers(@Param("currentDate") Date currentDate);
    Voucher findVoucherByCodeAndIsDeletedFalse(String code);
}
