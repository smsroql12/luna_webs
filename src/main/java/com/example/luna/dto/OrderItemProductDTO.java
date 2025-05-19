package com.example.luna.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemProductDTO {
    private Long orderListId; // order_list PK (선택)
    private Long productNo;
    private String productName;
    private Integer productPrice;
    private Integer quantity; // 주문 수량
    // 생성자, getter/setter
}
