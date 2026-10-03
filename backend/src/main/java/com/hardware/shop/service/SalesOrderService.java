package com.hardware.shop.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hardware.shop.common.BizException;
import com.hardware.shop.common.Dict;
import com.hardware.shop.entity.Customer;
import com.hardware.shop.entity.OrderItem;
import com.hardware.shop.entity.Product;
import com.hardware.shop.entity.SalesOrder;
import com.hardware.shop.repository.CustomerRepository;
import com.hardware.shop.repository.OrderItemRepository;
import com.hardware.shop.repository.ProductRepository;
import com.hardware.shop.repository.SalesOrderRepository;

@Service
public class SalesOrderService {

    private final SalesOrderRepository orderRepo;
    private final OrderItemRepository itemRepo;
    private final ProductRepository productRepo;
    private final CustomerRepository customerRepo;

    public SalesOrderService(SalesOrderRepository orderRepo, OrderItemRepository itemRepo,
            ProductRepository productRepo, CustomerRepository customerRepo) {
        this.orderRepo = orderRepo;
        this.itemRepo = itemRepo;
        this.productRepo = productRepo;
        this.customerRepo = customerRepo;
    }

    public static class OrderInput {
        public Long customerId;
        public String step;
        public String status;
    }

    public static class ItemInput {
        public Long productId;
        public Integer qty;
    }

    public List<SalesOrder> list(String status) {
        if (status != null) {
            return orderRepo.findByStatus(status);
        }
        return orderRepo.findAll();
    }

    public SalesOrder get(Long id) {
        return orderRepo.findById(id).orElseThrow(() -> new BizException("开单不存在"));
    }

    public List<OrderItem> items(Long orderId) {
        if (!orderRepo.existsById(orderId)) {
            throw new BizException("开单不存在");
        }
        return itemRepo.findByOrderId(orderId);
    }

    @Transactional
    public SalesOrder create(OrderInput in) {
        SalesOrder o = new SalesOrder();
        o.customerId = (in != null) ? in.customerId : null;
        if (o.customerId != null && !customerRepo.existsById(o.customerId)) {
            throw new BizException("客户不存在");
        }
        o.step = "选客";
        o.total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        o.status = "草稿";
        return orderRepo.save(o);
    }

    @Transactional
    public SalesOrder update(Long id, OrderInput in) {
        SalesOrder o = orderRepo.findById(id).orElseThrow(() -> new BizException("开单不存在"));
        if ("已结算".equals(o.status) || "已作废".equals(o.status)) {
            throw new BizException("该单已" + o.status + "，不能修改");
        }
        if (in.customerId != null) {
            if (!customerRepo.existsById(in.customerId)) {
                throw new BizException("客户不存在");
            }
            o.customerId = in.customerId;
        }
        if (in.step != null && !in.step.equals(o.step)) {
            if (!Dict.isValidOrderStep(in.step)) {
                throw new BizException("开单步骤必须是：选客/选型/确认/完成");
            }
            if (!Dict.canForwardStep(o.step, in.step)) {
                throw new BizException("开单步骤只能前进，不能后退");
            }
            o.step = in.step;
        }
        if (in.status != null) {
            if (!Dict.isValidSalesOrderStatus(in.status)) {
                throw new BizException("开单状态必须是：草稿/已结算/已作废");
            }
            o.status = in.status;
        }
        return orderRepo.save(o);
    }

    @Transactional
    public OrderItem addItem(Long orderId, ItemInput in) {
        SalesOrder o = orderRepo.findById(orderId).orElseThrow(() -> new BizException("开单不存在"));
        if (!"草稿".equals(o.status)) {
            throw new BizException("只有草稿单才能增减商品");
        }
        if (!"选型".equals(o.step) && !"确认".equals(o.step)) {
            throw new BizException("请先进入「选型」步骤再添加商品");
        }
        if (in.productId == null) {
            throw new BizException("请选择商品");
        }
        if (in.qty == null || in.qty <= 0) {
            throw new BizException("数量必须大于 0");
        }
        Product p = productRepo.findById(in.productId)
                .orElseThrow(() -> new BizException("商品不存在"));
        if (!"在售".equals(p.status)) {
            throw new BizException("商品「" + p.name + "」已停用，不能开单");
        }
        OrderItem item = new OrderItem();
        item.orderId = orderId;
        item.productId = in.productId;
        item.qty = in.qty;
        return itemRepo.save(item);
    }

    @Transactional
    public void removeItem(Long orderId, Long itemId) {
        SalesOrder o = orderRepo.findById(orderId).orElseThrow(() -> new BizException("开单不存在"));
        if (!"草稿".equals(o.status)) {
            throw new BizException("只有草稿单才能增减商品");
        }
        itemRepo.deleteByOrderIdAndItemId(orderId, itemId);
    }

    @Transactional
    public SalesOrder settle(Long id) {
        SalesOrder o = orderRepo.findById(id).orElseThrow(() -> new BizException("开单不存在"));
        if (!"草稿".equals(o.status)) {
            throw new BizException("该单已" + o.status + "，不能重复结算");
        }
        if (!"确认".equals(o.step)) {
            throw new BizException("请先进入「确认」步骤再结算");
        }
        if (o.customerId == null || !customerRepo.existsById(o.customerId)) {
            throw new BizException("请先在「选客」步骤指定客户");
        }
        List<OrderItem> items = itemRepo.findByOrderId(id);
        if (items.isEmpty()) {
            throw new BizException("订单没有商品，请先在「选型」步骤添加商品");
        }

        // 按商品聚合需要扣减的库存，避免同一商品多条明细重复校验
        Map<Long, Integer> need = new HashMap<>();
        for (OrderItem it : items) {
            need.merge(it.productId, it.qty, Integer::sum);
        }

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> e : need.entrySet()) {
            Product p = productRepo.findById(e.getKey())
                    .orElseThrow(() -> new BizException("商品不存在（ID=" + e.getKey() + "）"));
            if (!"在售".equals(p.status)) {
                throw new BizException("商品「" + p.name + "」已停用，不能开单");
            }
            if (p.stock < e.getValue()) {
                throw new BizException("商品「" + p.name + "」库存不足（剩 " + p.stock + "）");
            }
            total = total.add(p.price.multiply(BigDecimal.valueOf(e.getValue())));
        }

        Customer c = customerRepo.findById(o.customerId).orElseThrow(() -> new BizException("客户不存在"));
        if ("批发".equals(c.memberLevel)) {
            total = total.multiply(new BigDecimal("0.9")).setScale(2, RoundingMode.HALF_UP);
        } else {
            total = total.setScale(2, RoundingMode.HALF_UP);
        }

        // 全部校验通过后再扣减库存
        for (Map.Entry<Long, Integer> e : need.entrySet()) {
            Product p = productRepo.findById(e.getKey())
                    .orElseThrow(() -> new BizException("商品不存在（ID=" + e.getKey() + "）"));
            p.stock -= e.getValue();
            productRepo.save(p);
        }

        o.total = total;
        o.status = "已结算";
        o.step = "完成";
        return orderRepo.save(o);
    }

    @Transactional
    public SalesOrder voidOrder(Long id) {
        SalesOrder o = orderRepo.findById(id).orElseThrow(() -> new BizException("开单不存在"));
        if (!"草稿".equals(o.status)) {
            throw new BizException("只有草稿单才能作废");
        }
        o.status = "已作废";
        return orderRepo.save(o);
    }
}
