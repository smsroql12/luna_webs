package com.example.luna.form;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordUpdateForm {

    @NotEmpty(message = "Please enter your current password.")
    private String currentPassword;

    @NotEmpty(message = "Please enter a new password.")
    @Size(min = 10, message = "Password must be at least 10 characters.")
    @Pattern(regexp = ".*[!@#$%^&*(),.?\":{}|<>].*", message = "Must contain at least one special character.")
    private String newPassword;

    @NotEmpty(message = "Please enter a new password verification.")
    private String newPasswordConfirm;

    public boolean isNewPasswordMatching() {
        return newPassword.equals(newPasswordConfirm);
    }
}