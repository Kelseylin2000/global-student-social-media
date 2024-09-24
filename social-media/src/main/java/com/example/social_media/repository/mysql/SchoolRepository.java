package com.example.social_media.repository.mysql;

import com.example.social_media.model.entity.School;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolRepository extends JpaRepository<School, Long> {

    List<School> findAll();
    List<School> findByNation_NationId(Integer nationId);
    List<School> findByNameContaining(String name);
}