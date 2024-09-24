package com.example.social_media.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignInRequestDto {

    @NotBlank(message = "Provider is mandatory")
    @Size(max = 20, message = "Provider should not be longer than 20 characters")
    private String provider;

    @Email
    @Size(max = 100, message = "Email should not be longer than 100 characters")
    private String email;

    @Size(max = 20, message = "password should not be longer than 20 characters")
    private String password;

    private String accessToken;
}