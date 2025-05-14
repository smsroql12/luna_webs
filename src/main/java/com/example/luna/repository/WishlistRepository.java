package com.example.luna.repository;

import com.example.luna.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {
    List<Wishlist> findByEmailOrderByWishdateDesc(String email);

    Page<Wishlist> findByEmailOrderByWishdateDesc(String email, Pageable pageable);

    boolean existsByEmailAndProductid(String email, Integer productid);

    Optional<Wishlist> findByEmailAndProductid(String email, Integer productid);
}