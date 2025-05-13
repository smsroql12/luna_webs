package com.example.luna.repository;

import com.example.luna.entity.TableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TableRepository extends JpaRepository<TableEntity, Long> {
    @Query("SELECT b.id FROM TableEntity b")
    List<Long> findAllIds();
    List<TableEntity> findByActiveOrderByTbindexAsc(Integer active);

    @Query("SELECT t.name FROM TableEntity t WHERE t.id = :id")
    String findNameById(@Param("id") Long id);
}
