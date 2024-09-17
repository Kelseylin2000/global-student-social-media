package com.example.social_media.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    @JsonProperty("user_id")
    private Long userId;

    private String provider;

    private String name;

    private String email;

    private String password;

    @JsonProperty("from_school_id")
    private Long fromSchoolId;

    @JsonProperty("to_school_id")
    private Long toSchoolId;
}