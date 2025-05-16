package com.example.luna.service;

import com.example.luna.entity.SiteUser;
import com.example.luna.form.UserCreateForm;
import com.example.luna.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        SiteUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles("USER") // 필요 시 사용자 역할 추가
                .build();
    }

    public void updatePassword(String email, String newPassword) {
        SiteUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일의 사용자를 찾을 수 없습니다."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public SiteUser getByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }

    public void save(SiteUser user) {
        userRepository.save(user);
    }

    public SiteUser login(String email, String password) {
        SiteUser user = userRepository.findByEmail(email).orElse(null);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    public SiteUser create(UserCreateForm form) {
        SiteUser user = new SiteUser();
        user.setEmail(form.getEmail());
        user.setFirstname(form.getFirstname());
        user.setLastname(form.getLastname());
        user.setPassword(passwordEncoder.encode(form.getPassword1()));
        user.setAddress1(form.getAddress1());
        user.setAddress2(form.getAddress2());
        user.setPhone(form.getPhone());
        user.setRegdate(LocalDateTime.now()); //현재 시간으로 가입일 지정
        return userRepository.save(user);
    }

    public boolean isEmailExist(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public Page<SiteUser> searchUsers(String type, String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return userRepository.findAll(pageable);
        }

        switch (type) {
            case "email":
                return userRepository.findByEmailContainingIgnoreCase(search, pageable);
            case "firstname":
                return userRepository.findByFirstnameContainingIgnoreCase(search, pageable);
            case "lastname":
                return userRepository.findByLastnameContainingIgnoreCase(search, pageable);
            default:
                return userRepository.findAll(pageable);
        }
    }

    public SiteUser getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 ID의 사용자를 찾을 수 없습니다."));
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

}