package com.hardware.shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hardware.shop.entity.AfterSale;

public interface AfterSaleRepository extends JpaRepository<AfterSale, Long> {

    List<AfterSale> findByStatus(String status);

    List<AfterSale> findByOrderId(Long orderId);
}
