package com.example.social_media.mapper;

import com.example.social_media.dto.UserDto;
import com.example.social_media.model.entity.User;
import com.example.social_media.model.entity.School;

import java.time.LocalDateTime;

public class UserMapper {

    public static User convertToEntity(UserDto userDto, School fromSchool, School toSchool) {
        User user = new User();
        user.setUserId(userDto.getUserId());
        user.setProvider(userDto.getProvider());
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        user.setFromSchool(fromSchool);  
        user.setToSchool(toSchool);      
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return user;
    }

    public static UserDto convertToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setUserId(user.getUserId());
        userDto.setProvider(user.getProvider());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        userDto.setPassword(user.getPassword());
        userDto.setFromSchoolId(user.getFromSchool() != null ? user.getFromSchool().getSchoolId() : null);
        userDto.setToSchoolId(user.getToSchool() != null ? user.getToSchool().getSchoolId() : null);

        return userDto;
    }
}
