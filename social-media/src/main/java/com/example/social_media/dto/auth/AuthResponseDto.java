package com.example.social_media.dto.auth;

import com.example.social_media.dto.user.UserDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    private String accessToken;
    private Long accessExpired;
    private UserDto user;
}