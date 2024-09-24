package com.example.social_media.model.document;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "browsingHistory")
public class BrowsingHistory {

    @Id
    private String id;

    private Long userId;
    private String postId;
    private LocalDateTime timestamp;
}
