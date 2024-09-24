package com.example.social_media.dto.post;

import com.example.social_media.model.document.Post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoredPostDto {

    private Post post;
    private double score;
}
