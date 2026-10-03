package com.hardware.shop.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hardware.shop.common.BizException;
import com.hardware.shop.common.Dict;
import com.hardware.shop.entity.Product;
import com.hardware.shop.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public static class ProductInput {
        public String sku;
        public String name;
        public String category;
        public BigDecimal price;
        public Integer stock;
        public String status;
    }

    public List<Product> list(String category, String status) {
        if (category != null && status != null) {
            return repo.findByCategoryAndStatus(category, status);
        }
        if (category != null) {
            return repo.findByCategory(category);
        }
        if (status != null) {
            return repo.findByStatus(status);
        }
        return repo.findAll();
    }

    public Product get(Long id) {
        return repo.findById(id).orElseThrow(() -> new BizException("商品不存在"));
    }

    @Transactional
    public Product create(ProductInput in) {
        if (in.sku == null || in.sku.isBlank()) {
            throw new BizException("货号不能为空");
        }
        if (in.name == null || in.name.isBlank()) {
            throw new BizException("品名不能为空");
        }
        if (repo.existsBySku(in.sku)) {
            throw new BizException("货号「" + in.sku + "」已存在");
        }
        if (!Dict.isValidProductCategory(in.category)) {
            throw new BizException("商品类别必须是：工具/紧固件/电工/建材");
        }
        if (in.price == null || in.price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException("单价不能为负");
        }
        if (in.stock == null || in.stock < 0) {
            throw new BizException("库存不能为负");
        }
        Product p = new Product();
        p.sku = in.sku;
        p.name = in.name;
        p.category = in.category;
        p.price = in.price.setScale(2, RoundingMode.HALF_UP);
        p.stock = in.stock;
        p.status = (in.status == null) ? "在售" : in.status;
        if (!Dict.isValidProductStatus(p.status)) {
            throw new BizException("商品状态必须是：在售/停用");
        }
        return repo.save(p);
    }

    @Transactional
    public Product update(Long id, ProductInput in) {
        Product p = repo.findById(id).orElseThrow(() -> new BizException("商品不存在"));
        if (in.sku != null) {
            if (in.sku.isBlank()) {
                throw new BizException("货号不能为空");
            }
            if (!in.sku.equals(p.sku) && repo.existsBySku(in.sku)) {
                throw new BizException("货号「" + in.sku + "」已存在");
            }
            p.sku = in.sku;
        }
        if (in.name != null) {
            if (in.name.isBlank()) {
                throw new BizException("品名不能为空");
            }
            p.name = in.name;
        }
        if (in.category != null) {
            if (!Dict.isValidProductCategory(in.category)) {
                throw new BizException("商品类别必须是：工具/紧固件/电工/建材");
            }
            p.category = in.category;
        }
        if (in.price != null) {
            if (in.price.compareTo(BigDecimal.ZERO) < 0) {
                throw new BizException("单价不能为负");
            }
            p.price = in.price.setScale(2, RoundingMode.HALF_UP);
        }
        if (in.stock != null) {
            if (in.stock < 0) {
                throw new BizException("库存不能为负");
            }
            p.stock = in.stock;
        }
        if (in.status != null) {
            if (!Dict.isValidProductStatus(in.status)) {
                throw new BizException("商品状态必须是：在售/停用");
            }
            p.status = in.status;
        }
        return repo.save(p);
    }
}
