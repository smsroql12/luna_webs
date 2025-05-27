package com.example.luna.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemDto {
    private Long productid;
    private Float price;
    private Integer quantity;
    // getter/setter
}