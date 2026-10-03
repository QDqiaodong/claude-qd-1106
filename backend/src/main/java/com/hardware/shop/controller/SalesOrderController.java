package com.hardware.shop.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hardware.shop.common.ApiResponse;
import com.hardware.shop.entity.OrderItem;
import com.hardware.shop.entity.SalesOrder;
import com.hardware.shop.service.SalesOrderService;

@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {

    private final SalesOrderService svc;

    public SalesOrderController(SalesOrderService svc) {
        this.svc = svc;
    }

    @GetMapping
    public ApiResponse<List<SalesOrder>> list(@RequestParam(required = false) String status) {
        return ApiResponse.success(svc.list(status));
    }

    @PostMapping
    public ApiResponse<SalesOrder> create(@RequestBody(required = false) SalesOrderService.OrderInput in) {
        return ApiResponse.success(svc.create(in));
    }

    @PutMapping("/{id}")
    public ApiResponse<SalesOrder> update(@PathVariable Long id,
            @RequestBody SalesOrderService.OrderInput in) {
        return ApiResponse.success(svc.update(id, in));
    }

    @GetMapping("/{id}/items")
    public ApiResponse<List<OrderItem>> items(@PathVariable Long id) {
        return ApiResponse.success(svc.items(id));
    }

    @PostMapping("/{id}/items")
    public ApiResponse<OrderItem> addItem(@PathVariable Long id,
            @RequestBody SalesOrderService.ItemInput in) {
        return ApiResponse.success(svc.addItem(id, in));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ApiResponse<Void> removeItem(@PathVariable Long id, @PathVariable Long itemId) {
        svc.removeItem(id, itemId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/settle")
    public ApiResponse<SalesOrder> settle(@PathVariable Long id) {
        return ApiResponse.success(svc.settle(id));
    }

    @PostMapping("/{id}/void")
    public ApiResponse<SalesOrder> voidOrder(@PathVariable Long id) {
        return ApiResponse.success(svc.voidOrder(id));
    }
}
