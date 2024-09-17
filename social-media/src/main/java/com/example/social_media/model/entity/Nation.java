package com.example.social_media.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

@Entity
@Table(name = "nation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Nation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer nationId;

    private String name;
}
