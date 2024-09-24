package com.example.social_media.dto.user;

import java.util.List;

import com.example.social_media.model.node.UserNode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserNodeWithMutualInfoDto {

    UserNode target;

    private List<String> mutualFriends;
    private List<String> mutualInterests;
    private String relationship;
}
