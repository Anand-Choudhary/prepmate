package com.example.user_service.repository;

import com.example.user_service.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {

    @Query("SELECT e FROM Education e WHERE e.profile.id = :profileId")
    List<Education> findByProfileId(@Param("profileId") Long profileId);
}