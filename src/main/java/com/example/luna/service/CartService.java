package com.example.luna.service;

import com.example.luna.dto.CartItemDTO;
import com.example.luna.repository.CartRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
    private final CartRepository repo;

    public CartService(CartRepository repo) {
        this.repo = repo;
    }

    public List<CartItemDTO> getAllItemsForUser(String email) {
        return repo.findByEmail(email).stream()
                .map(item -> new CartItemDTO(
                        item.getId(),
                        item.getProductName(),
                        1, // 수량 필드가 없으면 일단 1개로 처리하거나 별도 계산 필요
                        0  // 가격 정보가 없으면 0 또는 별도 조회 필요
                ))
                .collect(Collectors.toList());
    }
}
