package com.lms.dto;

import com.lms.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @NotBlank(message = "Contact number is required")
    @Pattern(
            regexp = "^(?:\\+91|91|0)?[6-9]\\d{9}$",
            message = "Enter a valid Indian mobile number"
    )
    private String contactNumber;
    @NotNull(message = "Role is required")
    private Role role;

    @NotBlank(message = "Department is required")
    private String department;
}
