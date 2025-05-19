package com.example.luna.dto;

import com.example.luna.entity.Order;
import com.example.luna.entity.OrderItem;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderView {
    private Order order;
    private List<OrderViewItem> items;

    public OrderView(Order order, List<OrderViewItem> items) {
        this.order = order;
        this.items = items;
    }
}