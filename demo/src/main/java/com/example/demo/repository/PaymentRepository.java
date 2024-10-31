package com.example.demo.repository;

import com.example.demo.entity.Account;
import com.example.demo.entity.Consignment;
import com.example.demo.entity.Orders;
import com.example.demo.entity.Payment;
import com.example.demo.model.Response.PaymentResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Payment findByConsignment(Consignment consignment);

    Payment findByOrders(Orders orders);


    @Query("SELECT p FROM Payment p WHERE p.customer = :customer AND p.orders IS NOT NULL")
    List<Payment> findByCustomerWithOrders(@Param("customer") Account customer);

    @Query("SELECT p FROM Payment p WHERE p.consignment.id = :consignmentId")
    Payment findByConsignmentId(@Param("consignmentId") Long consignmentId);
}
