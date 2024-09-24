package com.example.social_media.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.social_media.dto.ApiResponseDto;
import com.example.social_media.dto.NationDto;
import com.example.social_media.service.NationService;

@RestController
@RequestMapping("/api/1.0/nations")
public class NationController {

    private final NationService nationService;

    public NationController(NationService nationService) {
        this.nationService = nationService;
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponseDto<List<NationDto>>> getAllNations() {
        List<NationDto> nations = nationService.getAllNations();
        return ResponseEntity.ok(new ApiResponseDto<>(nations));
    }
}
