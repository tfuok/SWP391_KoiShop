package com.example.demo.repository;

import com.example.demo.entity.Consignment;
import com.example.demo.entity.Orders;
import com.example.demo.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByConsignment(Consignment consignment);
    Payment findByOrders(Orders orders);
}
