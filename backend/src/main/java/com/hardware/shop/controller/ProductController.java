package com.hardware.shop.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hardware.shop.common.ApiResponse;
import com.hardware.shop.entity.Product;
import com.hardware.shop.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService svc;

    public ProductController(ProductService svc) {
        this.svc = svc;
    }

    @GetMapping
    public ApiResponse<List<Product>> list(@RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(svc.list(category, status));
    }

    @PostMapping
    public ApiResponse<Product> create(@RequestBody ProductService.ProductInput in) {
        return ApiResponse.success(svc.create(in));
    }

    @PutMapping("/{id}")
    public ApiResponse<Product> update(@PathVariable Long id,
            @RequestBody ProductService.ProductInput in) {
        return ApiResponse.success(svc.update(id, in));
    }
}
