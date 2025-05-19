package com.example.luna.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "`order`") // order는 예약어라 백틱 필수!
public class Order {
    @Id
    private String id;

    private Long userid;
    private Integer total;
    private Integer status;
    private String requests;
    private LocalDateTime created = LocalDateTime.now();
    private String trackingnum;
    private String shipcom;
    private String address1;
    private String address2;

    @ColumnDefault("0")
    @Column(nullable = false)
    private int cancel;

    @ColumnDefault("0")
    @Column(nullable = false)
    private int returnitem;

    private String returnmsg;

    private String returntrackingnum;

    @ColumnDefault("0")
    @Column(nullable = false)
    private int returncomplete;
    // 양방향 관계 필요 시 추가 가능
    // @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    // private List<OrderItem> items = new ArrayList<>();
}