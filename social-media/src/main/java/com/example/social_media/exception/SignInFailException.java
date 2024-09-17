package com.example.social_media.exception;

public class SignInFailException extends RuntimeException {
    public SignInFailException(String message) {
        super(message);
    }
}