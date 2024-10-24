package com.example.social_media.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.social_media.dto.ApiResponseDto;
import com.example.social_media.dto.ErrorResponseDto;
import com.example.social_media.dto.auth.AuthResponseDto;
import com.example.social_media.dto.auth.SignInRequestDto;
import com.example.social_media.dto.auth.SignUpRequestDto;
import com.example.social_media.exception.EmailAlreadyExistsException;
import com.example.social_media.exception.SignInFailException;
import com.example.social_media.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/1.0/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(value = "/signup", consumes = "application/json")
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> signUp(@Valid @RequestBody SignUpRequestDto signUpRequest) {
        AuthResponseDto reponse = authService.signUp(signUpRequest);
        return ResponseEntity.ok(new ApiResponseDto<>(reponse));
    }

    @PostMapping(value = "/signin", consumes = "application/json")
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> signIn(@Valid @RequestBody SignInRequestDto signInRequest) {
        AuthResponseDto reponse = authService.signIn(signInRequest);
        return ResponseEntity.ok(new ApiResponseDto<>(reponse));
    }

    // exception handler
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        ErrorResponseDto errorResponse = new ErrorResponseDto(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(SignInFailException.class)
    public ResponseEntity<ErrorResponseDto> handleSignInFailException(SignInFailException ex) {
        ErrorResponseDto errorResponse = new ErrorResponseDto(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
}
