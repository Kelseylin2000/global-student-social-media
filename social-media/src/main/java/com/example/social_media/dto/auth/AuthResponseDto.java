package com.example.social_media.dto.auth;

import com.example.social_media.dto.UserDto;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("access_expired")
    private Long accessExpired;
    private UserDto user;
}