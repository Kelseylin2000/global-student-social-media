package com.example.social_media.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequestDto {

    @NotBlank(message = "Name is mandatory")
    @Size(max = 30, message = "Name should not be longer than 30 characters")
    private String name;

    @Email
    @NotBlank(message = "Email is mandatory")
    @Size(max = 100, message = "Email should not be longer than 100 characters")
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Size(max = 20, message = "password should not be longer than 20 characters")
    private String password;
}