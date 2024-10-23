package com.example.demo.repository;

import com.example.demo.entity.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transactions, Long> {
    @Query("select year(t.createAt) as year, month(t.createAt) as month, sum(t.amount) " +
            "from Transactions t " +
            "where t.status='SUCCESS' and t.to.id =: userId " +
            "group by year(t.createAt),month(t.createAt) " +
            "order by year(t.createAt),month(t.createAt)")
    List<Object[]> calculateRevenue(@Param("userId") Long userId);
}
