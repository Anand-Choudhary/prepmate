package com.example.user_service.repository;

import com.example.user_service.entity.Projects;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectsRepository extends JpaRepository<Projects, Long> {

    @Query("SELECT p FROM Projects p WHERE p.profile.id = :profileId")
    List<Projects> findByProfileId(@Param("profileId") Long profileId);
}
