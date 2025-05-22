package com.example.luna.dto;

import jakarta.validation.constraints.NotEmpty;

public class PasswordForm {
    @NotEmpty(message = "비밀번호를 입력하세요.")
    private String currentPassword;

    // Getter, Setter
    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }
}