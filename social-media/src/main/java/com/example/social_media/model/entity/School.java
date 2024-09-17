package com.example.social_media.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "school")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long schoolId;

    private String name;

    @ManyToOne
    @JoinColumn(name = "nation_id")
    private Nation nation;

    private String emailDomain;

    @OneToMany(mappedBy = "fromSchool")
    private List<User> fromUsers;

    @OneToMany(mappedBy = "toSchool")
    private List<User> toUsers;
}