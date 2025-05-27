package com.example.luna.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CartItemDTO {
    private Long id;
    private String name;
    private int quantity;
    private Float price;
}
