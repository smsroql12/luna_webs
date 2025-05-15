package com.example.luna.repository;

import com.example.luna.entity.MainItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MainItemRepository extends JpaRepository<MainItemEntity, Long> {
    List<MainItemEntity> findByActiveOrderByMindexAsc(boolean active);
}
