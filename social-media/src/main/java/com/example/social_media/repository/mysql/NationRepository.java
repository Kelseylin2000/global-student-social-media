package com.example.social_media.repository.mysql;

import com.example.social_media.model.entity.Nation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NationRepository extends JpaRepository<Nation, Integer> {
}