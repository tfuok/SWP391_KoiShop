package com.example.demo.repository;

import com.example.demo.entity.Account;
import com.example.demo.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    //tìm account = email
    Account findAccountByEmailAndIsDeletedFalse(String email);

    Account findAccountByIdAndIsDeletedFalse(long id);

    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

//    List<Account> findAccountByIsDeletedFalse();

    List<Account> findAccountByIsDeletedFalseOrderByRole();

    List<Account> findByRoleAndIsDeletedFalse(Role role);

    List<Account> findByUsernameContainingAndRoleAndIsDeletedFalse(String name, Role role);

    Account findAccountByRole(Role role);

    Account findByUsername(String username);

    Account findAccountById(long id);

    @Query("select count(a) from Account a where a.role = :role")
    long countByRole(@Param("role") Role role);
}
