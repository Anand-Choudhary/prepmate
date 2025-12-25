package com.example.user_service.entity;


import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Projects extends BaseModel
{
    @Column(nullable = false)
    private String projectName;

    @Column(nullable = false,length = 1000)
    private String projectDescription;

    @Column(nullable = false,length = 500)
    private String projectTechStack;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private UserProfile profile;
}
