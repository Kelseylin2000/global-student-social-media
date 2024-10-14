package com.example.social_media.dto.user;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasicInfo implements Serializable {
    private String name;
    private String phase;
    private String originSchoolName;
    private String exchangeSchoolName;
    private Integer nationId;
}
