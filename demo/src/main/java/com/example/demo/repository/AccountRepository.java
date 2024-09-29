package com.example.demo.repository;

import com.example.demo.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    //tìm account = email
    Account findAccountByEmail(String email);

    Account findAccountById(long id);

    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    List<Account> findAccountByIsDeletedFalse();
}
