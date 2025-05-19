package com.example.luna.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdminOrderDTO {
    private String orderId;
    private Integer status;
    private String email;
    private LocalDateTime created;
    private Integer cancel;
    private Integer total;
    private String address1;
    private String address2;

    public AdminOrderDTO(String orderId, Integer status, String email, LocalDateTime created, Integer cancel, Integer total, String address1, String address2) {
        this.orderId = orderId;
        this.status = status;
        this.email = email;
        this.created = created;
        this.cancel = cancel;
        this.total = total;
        this.address1 = address1;
        this.address2 = address2;
    }
}

