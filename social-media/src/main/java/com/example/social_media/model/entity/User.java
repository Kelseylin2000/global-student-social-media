package com.example.social_media.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.example.social_media.model.enumtype.Phase;


@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String provider;

    private String name;

    private String email;

    private String password;

    private String fromSchoolEmail;

    private String toSchoolEmail;

    @ManyToOne
    @JoinColumn(name = "from_school_id")
    private School fromSchool;

    @ManyToOne
    @JoinColumn(name = "to_school_id")
    private School toSchool;

    @Enumerated(EnumType.STRING)
    private Phase phase;

    private LocalDateTime lastLogin;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}