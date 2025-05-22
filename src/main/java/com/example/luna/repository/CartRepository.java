package com.example.luna.repository;

import com.example.luna.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByEmailOrderByRegdateAsc(String email);
    Optional<Cart> findByEmailAndProductid(String email, Integer productid);
    List<Cart> findByEmail(String email);
    void deleteByEmail(String email);
}