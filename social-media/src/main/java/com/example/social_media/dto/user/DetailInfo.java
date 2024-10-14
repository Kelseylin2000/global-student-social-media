package com.example.social_media.dto.user;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailInfo implements Serializable {
    private String introduction;
    private List<String> interests;
}
