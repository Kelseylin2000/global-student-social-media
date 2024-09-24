package com.example.social_media.service;

import java.util.List;

import com.example.social_media.dto.SchoolDto;

public interface SchoolService {
    List<SchoolDto> getAllSchools();
    List<SchoolDto> getSchoolsByNationId(Integer nationId);
    List<SchoolDto> getSchoolsByName(String name);
    String getEmailDomainBySchoolId(Long schoolId);
}
