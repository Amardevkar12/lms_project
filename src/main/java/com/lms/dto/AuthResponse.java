package com.lms.dto;

import com.lms.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String name;
    private String email;
    private Role role;
    private String department;

    public AuthResponse(String token, Long id, String name, String email, Role role, String department) {
        this.token = token;
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.department = department;
    }
}
