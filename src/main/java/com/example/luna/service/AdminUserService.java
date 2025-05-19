package com.example.luna.service;

import com.example.luna.entity.AdminUser;
import com.example.luna.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AdminUserService {
    @Autowired
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AdminUser login(String adminid, String rawPassword) {
        Optional<AdminUser> optionalAdmin = adminUserRepository.findByAdminid(adminid);
        if (optionalAdmin.isEmpty()) {
            return null;
        }

        AdminUser admin = optionalAdmin.get();
        if (passwordEncoder.matches(rawPassword, admin.getPassword())) {
            return admin;
        } else {
            return null;
        }
    }


    public Page<AdminUser> searchAdmins(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return adminUserRepository.findByAdminidContainingAndRole(search, 0, pageable);
        } else {
            return adminUserRepository.findByRole(0, pageable);
        }
    }

    @Transactional
    public void deleteById(String adminid) {
        AdminUser admin = adminUserRepository.findByAdminid(adminid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 관리자입니다."));

        if (admin.getRole() == 1) {
            System.out.println("슈퍼 관리자는 삭제할 수 없습니다.");
            throw new IllegalArgumentException("슈퍼 관리자는 삭제할 수 없습니다.");
        }

        adminUserRepository.delete(admin);

    }
}
