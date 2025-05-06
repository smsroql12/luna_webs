package com.example.luna.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetForm {
    @NotBlank
    private String password1;

    @NotBlank
    private String password2;

    private String token;
}