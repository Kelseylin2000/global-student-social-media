package com.example.social_media.controller;

import com.example.social_media.dto.ApiResponseDto;
import com.example.social_media.dto.SchoolDto;
import com.example.social_media.service.SchoolService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/1.0/schools")
public class SchoolController {

    private final SchoolService schoolService;

    public SchoolController(SchoolService schoolService) {
        this.schoolService = schoolService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<SchoolDto>>> getSchools(
        @RequestParam(required = false) Integer nationId,
        @RequestParam(required = false) String name) {
        
        List<SchoolDto> schoolList;
        
        if (nationId != null) {
            schoolList = schoolService.getSchoolsByNationId(nationId);
        } else if (name != null) {
            schoolList = schoolService.getSchoolsByName(name);
        } else {
            schoolList = schoolService.getAllSchools();
        }
        
        return ResponseEntity.ok(new ApiResponseDto<>(schoolList));
    }

    @GetMapping("/{schoolId}/email-domain")
    public ResponseEntity<ApiResponseDto<String>> getSchoolEmailDomain(@PathVariable Long schoolId) {
        String emailDomain = schoolService.getEmailDomainBySchoolId(schoolId);
        return ResponseEntity.ok(new ApiResponseDto<>(emailDomain));
    }
}
