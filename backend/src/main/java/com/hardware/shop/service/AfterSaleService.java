package com.hardware.shop.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hardware.shop.common.BizException;
import com.hardware.shop.common.Dict;
import com.hardware.shop.entity.AfterSale;
import com.hardware.shop.entity.OrderItem;
import com.hardware.shop.entity.Product;
import com.hardware.shop.entity.SalesOrder;
import com.hardware.shop.repository.AfterSaleRepository;
import com.hardware.shop.repository.OrderItemRepository;
import com.hardware.shop.repository.ProductRepository;
import com.hardware.shop.repository.SalesOrderRepository;

@Service
public class AfterSaleService {

    private final AfterSaleRepository repo;
    private final SalesOrderRepository orderRepo;
    private final OrderItemRepository itemRepo;
    private final ProductRepository productRepo;

    public AfterSaleService(AfterSaleRepository repo, SalesOrderRepository orderRepo,
            OrderItemRepository itemRepo, ProductRepository productRepo) {
        this.repo = repo;
        this.orderRepo = orderRepo;
        this.itemRepo = itemRepo;
        this.productRepo = productRepo;
    }

    public static class AfterSaleInput {
        public Long orderId;
        public String reason;
        public String handle;
    }

    public List<AfterSale> list(String status) {
        if (status != null) {
            return repo.findByStatus(status);
        }
        return repo.findAll();
    }

    public AfterSale get(Long id) {
        return repo.findById(id).orElseThrow(() -> new BizException("售后单不存在"));
    }

    @Transactional
    public AfterSale create(AfterSaleInput in) {
        if (in.orderId == null) {
            throw new BizException("请选择关联开单");
        }
        SalesOrder o = orderRepo.findById(in.orderId).orElseThrow(() -> new BizException("开单不存在"));
        if (!"已结算".equals(o.status)) {
            throw new BizException("只能对已结算的开单创建售后");
        }
        if (!Dict.isValidAfterSaleHandle(in.handle)) {
            throw new BizException("处理方式必须是：维修/退换/作废");
        }
        AfterSale a = new AfterSale();
        a.orderId = in.orderId;
        a.reason = in.reason;
        a.handle = in.handle;
        a.status = "待处理";
        return repo.save(a);
    }

    @Transactional
    public AfterSale process(Long id) {
        AfterSale a = repo.findById(id).orElseThrow(() -> new BizException("售后单不存在"));
        if (!Dict.canProcessAfterSale(a.status)) {
            throw new BizException("该售后已处理，不能重复处理");
        }
        // 退换：按订单明细把库存加回对应商品
        if ("退换".equals(a.handle)) {
            List<OrderItem> items = itemRepo.findByOrderId(a.orderId);
            Map<Long, Integer> back = new HashMap<>();
            for (OrderItem it : items) {
                back.merge(it.productId, it.qty, Integer::sum);
            }
            for (Map.Entry<Long, Integer> e : back.entrySet()) {
                Product p = productRepo.findById(e.getKey())
                        .orElseThrow(() -> new BizException("商品不存在（ID=" + e.getKey() + "）"));
                p.stock += e.getValue();
                productRepo.save(p);
            }
        }
        a.status = "已处理";
        return repo.save(a);
    }
}
