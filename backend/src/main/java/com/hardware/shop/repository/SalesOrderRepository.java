package com.hardware.shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hardware.shop.entity.SalesOrder;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    List<SalesOrder> findByStatus(String status);
}
