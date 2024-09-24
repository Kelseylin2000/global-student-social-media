package com.example.social_media.service;

import com.example.social_media.dto.auth.AuthResponseDto;
import com.example.social_media.dto.auth.SignInRequestDto;
import com.example.social_media.dto.auth.SignUpRequestDto;

public interface AuthService {
    AuthResponseDto signUp(SignUpRequestDto signUpRequest);
    AuthResponseDto signIn(SignInRequestDto signInRequest);
    Long getCurrentUserId();
}
