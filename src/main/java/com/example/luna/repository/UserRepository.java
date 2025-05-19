package com.example.luna.repository;

import com.example.luna.entity.SiteUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<SiteUser, Long> {
    @Query("SELECT u.id FROM SiteUser u WHERE u.email = :email")
    Long findIdByEmail(@Param("email") String email);

    @Query("SELECT u FROM SiteUser u WHERE u.email LIKE %:email%")
    List<SiteUser> findByEmailLike(@Param("email") String email);

    Optional<SiteUser> findByEmail(String email);
    Page<SiteUser> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    Page<SiteUser> findByFirstnameContainingIgnoreCase(String firstname, Pageable pageable);
    Page<SiteUser> findByLastnameContainingIgnoreCase(String lastname, Pageable pageable);
}