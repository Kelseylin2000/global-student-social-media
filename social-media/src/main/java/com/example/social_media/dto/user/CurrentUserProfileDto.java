package com.example.social_media.dto.user;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserProfileDto {

    private Long userId;
    private String name;
    private String phase;
    private String originSchoolName;
    private String exchangeSchoolName;

    private String introduction;
    private List<String> interests;

    private String fromSchoolEmail;
    private String toSchoolEmail;

    private List<String> interestedSchools;
}
