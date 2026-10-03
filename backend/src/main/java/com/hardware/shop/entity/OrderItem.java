package com.hardware.shop.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "order_id", nullable = false)
    public Long orderId;

    @Column(name = "product_id", nullable = false)
    public Long productId;

    @Column(name = "qty", nullable = false)
    public Integer qty;

    @Column(name = "created_at")
    public LocalDateTime createdAt = LocalDateTime.now();
}
