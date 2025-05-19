package com.example.luna.repository;

import com.example.luna.dto.OrderItemProductDTO;
import com.example.luna.dto.OrderViewItem;
import com.example.luna.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderid(String orderid);
    @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.productid WHERE oi.orderid = :orderId")
    List<OrderItem> findWithProductByOrderid(String orderId);
    List<OrderItem> findByOrderidIn(List<String> orderids);

    @Query("SELECT new com.example.luna.dto.OrderViewItem(oi, p) " +
            "FROM OrderItem oi JOIN Product p ON oi.productid = p.no " +
            "WHERE oi.orderid = :orderId")
    List<OrderViewItem> findItemsWithProductByOrderId(@Param("orderId") String orderId);
}
