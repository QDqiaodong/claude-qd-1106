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
import com.hardware.shop.entity.Customer;
import com.hardware.shop.service.CustomerService;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService svc;

    public CustomerController(CustomerService svc) {
        this.svc = svc;
    }

    @GetMapping
    public ApiResponse<List<Customer>> list(@RequestParam(required = false) String memberLevel) {
        return ApiResponse.success(svc.list(memberLevel));
    }

    @PostMapping
    public ApiResponse<Customer> create(@RequestBody CustomerService.CustomerInput in) {
        return ApiResponse.success(svc.create(in));
    }

    @PutMapping("/{id}")
    public ApiResponse<Customer> update(@PathVariable Long id,
            @RequestBody CustomerService.CustomerInput in) {
        return ApiResponse.success(svc.update(id, in));
    }
}
