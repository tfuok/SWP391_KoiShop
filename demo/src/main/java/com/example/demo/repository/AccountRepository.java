package com.example.demo.repository;

import com.example.demo.entity.Account;
import com.example.demo.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    //tìm account = email
    Account findAccountByEmail(String email);

    Account findAccountByIdAndIsDeletedFalse(Long id);

    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

//    List<Account> findAccountByIsDeletedFalse();

    @Query("SELECT a FROM Account a WHERE a.isDeleted = false ORDER BY " +
            "CASE WHEN a.role = 'MANAGER' THEN 1 " +
            "WHEN a.role = 'STAFF' THEN 2 " +
            "ELSE 3 END")
    List<Account> findAccountByIsDeletedFalseOrderedByRole();

    List<Account> findByRole(Role role);

    List<Account> findByUsernameContainingAndRole(String name, Role role);

}
