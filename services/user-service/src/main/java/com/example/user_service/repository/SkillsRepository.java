package com.example.user_service.repository;

import com.example.user_service.entity.Skills;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkillsRepository extends JpaRepository<Skills, Long> {

    Optional<Skills> findByName(String name);

    boolean existsByName(String name);
}