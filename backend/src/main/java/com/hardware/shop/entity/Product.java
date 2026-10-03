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
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "sku", nullable = false, unique = true, length = 32)
    public String sku;

    @Column(name = "name", nullable = false, length = 128)
    public String name;

    @Column(name = "category", nullable = false, length = 16)
    public String category;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    public BigDecimal price;

    @Column(name = "stock", nullable = false)
    public Integer stock;

    @Column(name = "status", nullable = false, length = 16)
    public String status;

    @Column(name = "created_at")
    public LocalDateTime createdAt = LocalDateTime.now();
}
