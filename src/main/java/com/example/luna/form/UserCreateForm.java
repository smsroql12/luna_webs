package com.example.luna.form;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class UserCreateForm {
    @NotEmpty(message = "이메일은 필수 항목입니다.")
    @Email
    private String email;

    @NotEmpty(message = "firstname 필수 항목입니다.")
    private String firstname;

    @NotEmpty(message = "lastname 필수 항목입니다.")
    private String lastname;

    @NotEmpty(message = "비밀번호는 필수 항목입니다.")
    @Size(min = 10, message = "비밀번호는 10자 이상이어야 합니다.")
    @Pattern(regexp = ".*[!@#$%^&*()].*", message = "특수문자를 최소 하나 포함해야 합니다.")
    private String password1;

    @NotEmpty(message = "비밀번호 확인은 필수 항목입니다.")
    private String password2;

    @NotEmpty(message = "주소1은 필수 항목입니다.")
    private String address1;

    @NotEmpty(message = "주소2는 필수 항목입니다.")
    private String address2;

    @NotEmpty(message = "전화번호는 필수 항목입니다.")
    private String phone;

    public boolean isPasswordMatching() {
        return password1.equals(password2);
    }
}