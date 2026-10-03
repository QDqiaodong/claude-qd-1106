package com.hardware.shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hardware.shop.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    void deleteByOrderId(Long orderId);

    @Modifying
    @Query("DELETE FROM OrderItem i WHERE i.orderId = :orderId AND i.id = :itemId")
    void deleteByOrderIdAndItemId(@Param("orderId") Long orderId, @Param("itemId") Long itemId);
}
