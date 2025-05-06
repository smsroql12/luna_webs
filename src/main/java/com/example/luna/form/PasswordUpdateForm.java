package com.example.luna.form;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordUpdateForm {

    @NotEmpty(message = "현재 비밀번호를 입력하세요.")
    private String currentPassword;

    @NotEmpty(message = "새 비밀번호를 입력하세요.")
    @Size(min = 10, message = "비밀번호는 10자 이상이어야 합니다.")
    @Pattern(regexp = ".*[!@#$%^&*(),.?\":{}|<>].*", message = "특수문자를 하나 이상 포함해야 합니다.")
    private String newPassword;

    @NotEmpty(message = "새 비밀번호 확인을 입력하세요.")
    private String newPasswordConfirm;

    public boolean isNewPasswordMatching() {
        return newPassword.equals(newPasswordConfirm);
    }
}