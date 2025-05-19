package com.example.luna;

import com.example.luna.entity.AdminUser;
import com.example.luna.repository.AdminUserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Component
public class SuperAdminInitializer {

    @Autowired
    private AdminUserRepository adminUserRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void createSuperAdmin() throws IOException {
        File file = new File("super.txt");
        if (!file.exists()) {
            System.out.println("super.txt 파일이 존재하지 않습니다.");
            return;
        }

        List<String> lines = Files.readAllLines(file.toPath());
        if (lines.size() < 2) {
            System.out.println("super.txt 파일에 아이디 또는 비밀번호가 부족합니다.");
            return;
        }

        String adminid = lines.get(0).trim();
        String password = lines.get(1).trim();

        if (adminUserRepository.findByAdminid(adminid).isPresent()) {
            System.out.println("이미 사용중인 계정입니다.");
            return;
        }

        AdminUser adminUser = new AdminUser();
        adminUser.setAdminid(adminid);
        adminUser.setPassword(passwordEncoder.encode(password));
        adminUser.setRole(1); // 슈퍼관리자

        adminUserRepository.save(adminUser);
        System.out.println("슈퍼관리자 계정이 생성되었습니다.");
    }
}

