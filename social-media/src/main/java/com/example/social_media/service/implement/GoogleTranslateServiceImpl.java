package com.example.social_media.service.implement;

import org.springframework.stereotype.Service;

import com.example.social_media.service.GoogleTranslateService;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

@Service
public class GoogleTranslateServiceImpl implements GoogleTranslateService{

    private final Translate translate;

    public GoogleTranslateServiceImpl() {
        String apiKey = System.getenv("GOOGLE_API_KEY");
        this.translate = TranslateOptions.newBuilder().setApiKey(apiKey).build().getService();    }

    @Override
    public String translateText(String text, String targetLanguage) {
        Translation translation = translate.translate(text, Translate.TranslateOption.targetLanguage(targetLanguage));
        return translation.getTranslatedText();
    }
}
