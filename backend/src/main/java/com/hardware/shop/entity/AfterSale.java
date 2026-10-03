package com.hardware.shop.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "after_sale")
public class AfterSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "order_id", nullable = false)
    public Long orderId;

    @Column(name = "reason", length = 256)
    public String reason;

    @Column(name = "handle", nullable = false, length = 16)
    public String handle;

    @Column(name = "status", nullable = false, length = 16)
    public String status;

    @Column(name = "created_at")
    public LocalDateTime createdAt = LocalDateTime.now();
}
