package com.hardware.shop.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hardware.shop.common.ApiResponse;
import com.hardware.shop.entity.AfterSale;
import com.hardware.shop.service.AfterSaleService;

@RestController
@RequestMapping("/api/after-sales")
public class AfterSaleController {

    private final AfterSaleService svc;

    public AfterSaleController(AfterSaleService svc) {
        this.svc = svc;
    }

    @GetMapping
    public ApiResponse<List<AfterSale>> list(@RequestParam(required = false) String status) {
        return ApiResponse.success(svc.list(status));
    }

    @PostMapping
    public ApiResponse<AfterSale> create(@RequestBody AfterSaleService.AfterSaleInput in) {
        return ApiResponse.success(svc.create(in));
    }

    @PostMapping("/{id}/process")
    public ApiResponse<AfterSale> process(@PathVariable Long id) {
        return ApiResponse.success(svc.process(id));
    }
}
