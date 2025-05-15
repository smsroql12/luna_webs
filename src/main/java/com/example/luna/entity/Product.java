package com.example.luna.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long no;

    private String name;
    private String description;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer category;

    private String image;

    private int price;

    private boolean hot;
    private boolean sale;
    private boolean soldout;

    private int saleprice;

    @Column(name = "endsaledate")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endsaledate;

    @Column(name = "regdate")
    private LocalDateTime regdate;
}