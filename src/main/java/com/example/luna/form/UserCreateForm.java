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
    @NotEmpty(message = "Email is a mandatory item.")
    @Email
    private String email;

    @NotEmpty(message = "Firstname is a mandatory item.")
    private String firstname;

    @NotEmpty(message = "Lastname is a mandatory item.")
    private String lastname;

    @NotEmpty(message = "Password is a mandatory item.")
    @Size(min = 10, message = "Password must be at least 10 characters long.")
    @Pattern(regexp = ".*[!@#$%^&*()].*", message = "Must contain at least one special character.")
    private String password1;

    @NotEmpty(message = "Password verification is mandatory.")
    private String password2;

    @NotEmpty(message = "Country is a mandatory item.")
    private String address1;

    @NotEmpty(message = "Address is a mandatory item.")
    private String address2;

    @NotEmpty(message = "Phone number is a mandatory item.")
    private String phone;

    public boolean isPasswordMatching() {
        return password1 != null && password1.equals(password2);
    }
}