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
    private Float total;
    private String address1;
    private String address2;
    private Integer returnitem;
    private String returnmsg;
    private String returntrackingnum;
    private Integer returncomplete;

    public AdminOrderDTO(String orderId, Integer status, String email, LocalDateTime created, Integer cancel, Float total, String address1, String address2, Integer returnitem, String returnmsg, String returntrackingnum, Integer returncomplete) {
        this.orderId = orderId;
        this.status = status;
        this.email = email;
        this.created = created;
        this.cancel = cancel;
        this.total = total;
        this.address1 = address1;
        this.address2 = address2;
        this.returnitem = returnitem;
        this.returnmsg = returnmsg;
        this.returntrackingnum = returntrackingnum;
        this.returncomplete = returncomplete;
    }
}

