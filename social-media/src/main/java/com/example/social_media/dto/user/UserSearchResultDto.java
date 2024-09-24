package com.example.social_media.dto.user;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchResultDto {

    private Long userId;
    private String name;
    private String phase;

    private Long originSchoolId;
    private String originSchoolName;
    private Long exchangeSchoolId;
    private String exchangeSchoolName;

    private List<String> mutualFriends;
    private List<String> mutualInterests;
}
