package com.example.demo.repository;

import com.example.demo.entity.Account;
import com.example.demo.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Orders, Long> {
    List<Orders> findOrderssByCustomer(Account customer);
    Orders findByIdAndCustomer(long id, Account customer);
}
