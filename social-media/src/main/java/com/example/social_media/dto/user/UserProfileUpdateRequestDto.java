package com.example.social_media.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequestDto {

    private String name;
    private String introduction;
    private List<Integer> interests;
}