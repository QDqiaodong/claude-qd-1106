package com.hardware.shop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hardware.shop.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Product> findByCategory(String category);

    List<Product> findByStatus(String status);

    List<Product> findByCategoryAndStatus(String category, String status);
}
