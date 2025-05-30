package com.example.luna.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordResetForm {
    @NotBlank
    @NotEmpty(message = "Password is a mandatory item.")
    @Size(min = 10, message = "Password must be at least 10 characters long.")
    @Pattern(regexp = ".*[!@#$%^&*()].*", message = "Must contain at least one special character.")
    private String password1;

    @NotBlank
    @NotEmpty(message = "Password verification is mandatory.")
    private String password2;

    private String token;

    public boolean isPasswordMatching() {
        return password1.equals(password2);
    }
}