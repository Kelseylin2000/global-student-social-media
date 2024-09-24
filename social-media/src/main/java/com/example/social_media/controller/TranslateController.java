package com.example.social_media.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.social_media.service.GoogleTranslateService;

@RestController
@RequestMapping("/api/1.0/translate")
public class TranslateController {

    private final GoogleTranslateService googleTranslateService;

    public TranslateController(GoogleTranslateService googleTranslateService) {
        this.googleTranslateService = googleTranslateService;
    }

    @GetMapping("/text")
    public String translate(@RequestParam String text, @RequestParam String lang) {
        return googleTranslateService.translateText(text, lang);
    }
}
