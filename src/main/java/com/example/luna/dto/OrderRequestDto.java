package com.example.luna.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequestDto {
    private List<OrderItemDto> items;
    private Integer total;
    private String requests;
    private String address1;
    private String address2;
}