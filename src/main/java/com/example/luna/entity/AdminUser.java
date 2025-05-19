package com.example.luna.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@Setter
@Table(name = "admin_user")
public class AdminUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "no")
    private Long no;

    @Column(name = "adminid", unique = true, nullable = false)
    private String adminid;

    @Column(nullable = false)
    private String password;

    @ColumnDefault("0")
    @Column(nullable = false)
    private int role;
}
