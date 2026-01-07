package com.example.user_service.repository;

import com.example.user_service.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    @Query("SELECT e FROM Experience e WHERE e.profile.id = :profileId")
    List<Experience> findByProfileId(@Param("profileId") Long profileId);
}