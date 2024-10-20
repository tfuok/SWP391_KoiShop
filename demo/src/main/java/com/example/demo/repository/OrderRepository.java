package com.example.demo.repository;

import com.example.demo.entity.Account;
import com.example.demo.entity.Orders;
import com.example.demo.model.Response.OrderResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Orders, Long> {
    List<Orders> findOrderssByCustomer(Account customer);
    Orders findByIdAndCustomer(long id, Account customer);
}
