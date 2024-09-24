package com.example.social_media.service.implement;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.social_media.dto.SchoolDto;
import com.example.social_media.model.entity.School;
import com.example.social_media.repository.mysql.SchoolRepository;
import com.example.social_media.service.SchoolService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class SchoolServiceImpl implements SchoolService{

    private final SchoolRepository schoolRepository;

    public SchoolServiceImpl(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
    }

    @Override
    public List<SchoolDto> getAllSchools() {
        return convertToDto(schoolRepository.findAll());
    }

    @Override
    public List<SchoolDto> getSchoolsByNationId(Integer nationId) {
        return convertToDto(schoolRepository.findByNation_NationId(nationId));
    }

    @Override
    public List<SchoolDto> getSchoolsByName(String name) {
        return convertToDto(schoolRepository.findByNameContaining(name));
    }

    @Override
    public String getEmailDomainBySchoolId(Long schoolId) {
        return schoolRepository.findById(schoolId)
                .map(School::getEmailDomain)
                .orElseThrow(() -> new EntityNotFoundException("School not found with id: " + schoolId));
    }

    private List<SchoolDto> convertToDto(List<School> schools) {
        return schools.stream()
                      .map(school -> new SchoolDto(school.getSchoolId(), school.getName()))
                      .collect(Collectors.toList());
    }
}
