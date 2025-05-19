package com.example.luna.dto;

import com.example.luna.entity.OrderItem;
import com.example.luna.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OrderViewItem {
    private OrderItem orderItem;
    private Product product;
}