package com.hardware.shop.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "name", nullable = false, length = 64)
    public String name;

    @Column(name = "phone", nullable = false, unique = true, length = 24)
    public String phone;

    @Column(name = "member_level", nullable = false, length = 16)
    public String memberLevel;

    @Column(name = "created_at")
    public LocalDateTime createdAt = LocalDateTime.now();
}
