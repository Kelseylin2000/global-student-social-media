package com.example.social_media.service;

import com.example.social_media.dto.auth.AuthResponseDto;
import com.example.social_media.dto.auth.SignInRequestDto;
import com.example.social_media.dto.auth.SignUpRequestDto;

public interface UserService {
    AuthResponseDto signUp(SignUpRequestDto signUpRequest);
    AuthResponseDto signIn(SignInRequestDto signInRequest);
    String getUserNameByUserId(Long userId);
}
