package com.lms.controller;

import com.lms.dto.ApiResponse;
import com.lms.entity.User;
import com.lms.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*", "https://lms-frotend.vercel.app", "https://*.vercel.app"})
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    // ================= GET ALL USERS =================
    @GetMapping
    public ResponseEntity<ApiResponse> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(new ApiResponse(true, "Users fetched", users));
    }

    // ================= GET USER BY ID =================
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(new ApiResponse(true, "User found", user));
    }

    // ================= ADD USER =================
    @PostMapping
    public ResponseEntity<ApiResponse> addUser(@RequestBody User user) {
        User saved = userService.saveUser(user);
        return ResponseEntity.status(201)
                .body(new ApiResponse(true, "User added", saved));
    }

    // ================= UPDATE USER =================
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        User updated = userService.saveUser(user);
        return ResponseEntity.ok(new ApiResponse(true, "User updated", updated));
    }

    // ================= DELETE USER =================
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponse(true, "User deleted", null));
    }
}
