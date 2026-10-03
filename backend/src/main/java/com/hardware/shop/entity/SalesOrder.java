package com.hardware.shop.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sales_order")
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "customer_id")
    public Long customerId;

    @Column(name = "step", nullable = false, length = 16)
    public String step;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    public BigDecimal total;

    @Column(name = "status", nullable = false, length = 16)
    public String status;

    @Column(name = "created_at")
    public LocalDateTime createdAt = LocalDateTime.now();
}
