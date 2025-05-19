package com.example.luna.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "order_item")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderid; // FK지만 연관관계 매핑 안함 (필요 시 @ManyToOne 사용)

    private Long productid;

    private Integer quantity;

    private Integer price;
}
