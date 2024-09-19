package com.example.social_media.controller;

import org.springframework.web.bind.annotation.RequestMapping;

import com.example.social_media.dto.ApiResponseDto;
import com.example.social_media.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@Controller
@RequestMapping("/api/1.0/users")
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}/username")
    public ResponseEntity<ApiResponseDto<String>> getUserNameByUserId(@PathVariable Long userId) {
        String userName = userService.getUserNameByUserId(userId);
        if (userName != null) {
            return ResponseEntity.ok(new ApiResponseDto<>(userName));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}