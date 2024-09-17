package com.example.social_media.controller;

import org.springframework.web.bind.annotation.RequestMapping;

import com.example.social_media.dto.ApiResponseDto;
import com.example.social_media.dto.ErrorResponseDto;
import com.example.social_media.dto.auth.AuthResponseDto;
import com.example.social_media.dto.auth.SignInRequestDto;
import com.example.social_media.dto.auth.SignUpRequestDto;
import com.example.social_media.exception.EmailAlreadyExistsException;
import com.example.social_media.exception.SignInFailException;
import com.example.social_media.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/api/1.0/user")
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/signup", consumes = "application/json")
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> signUp(@Valid @RequestBody SignUpRequestDto signUpRequest) {
        AuthResponseDto reponse = userService.signUp(signUpRequest);
        return ResponseEntity.ok(new ApiResponseDto<>(reponse));
    }

    @PostMapping(value = "/signin", consumes = "application/json")
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> signIn(@Valid @RequestBody SignInRequestDto signInRequest) {
        AuthResponseDto reponse = userService.signIn(signInRequest);
        return ResponseEntity.ok(new ApiResponseDto<>(reponse));
    }

    @GetMapping("/getUserNameByUserId")
    public ResponseEntity<ApiResponseDto<String>> getUserNameByUserId(@RequestParam Long userId) {
        String userName = userService.getUserNameByUserId(userId);
        if (userName != null) {
            return ResponseEntity.ok(new ApiResponseDto<> (userName));
        } else {
            return ResponseEntity.notFound().build();
        }
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