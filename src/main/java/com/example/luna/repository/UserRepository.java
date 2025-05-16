package com.example.luna.repository;

import com.example.luna.entity.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<SiteUser, Long> {
    Optional<SiteUser> findByEmail(String email);
    Page<SiteUser> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    Page<SiteUser> findByFirstnameContainingIgnoreCase(String firstname, Pageable pageable);
    Page<SiteUser> findByLastnameContainingIgnoreCase(String lastname, Pageable pageable);
}