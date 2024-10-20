package com.example.demo.repository;

import com.example.demo.entity.Account;
import com.example.demo.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Orders, Long> {
    List<Orders> findOrdersByCustomer(Account customer);
    Orders findByIdAndCustomer(long id, Account customer);
    // Find all orders by a specific customer account
    List<Orders> findByCustomer(Account customer);

    // Find all orders assigned to a specific staff account
    List<Orders> findByStaff(Account staff);
}
