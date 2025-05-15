package com.example.luna.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "mainitem")
@Getter
@Setter
public class MainItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long no;

    @Column(name = "itemcode")
    private int itemcode;

    @Column(name = "mindex")
    private int mindex;

    @Column(name = "active")
    private boolean active;

    @Transient
    private String tempName;

    @Transient
    private String tempImage;
}