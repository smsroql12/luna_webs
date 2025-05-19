package com.example.luna.repository;

import com.example.luna.entity.AdminUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUser, String> {
    // 검색어 있는 경우
    Page<AdminUser> findByAdminidContainingAndRole(String adminid, int role, Pageable pageable);
    // 검색어 없는 경우
    Page<AdminUser> findByRole(int role, Pageable pageable);
    Optional<AdminUser> findByAdminid(String adminid);
    boolean existsByAdminid(String adminid);

}
