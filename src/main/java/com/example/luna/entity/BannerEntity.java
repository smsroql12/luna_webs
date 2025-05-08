package com.example.luna.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "banner_list")
public class BannerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(columnDefinition = "TEXT", name = "link")
    private String link;

    @Column(name = "image")
    private String image;

    @Column(name = "bindex")
    private Integer bindex;

    @Column(name = "bactive")
    private Boolean bactive;
}