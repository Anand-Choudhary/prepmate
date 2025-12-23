package com.example.user_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "user_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(
        callSuper = true,
        exclude = {
        "user", "education", "projects", "experience", "skills", "certifications"
})
public class UserProfile extends BaseModel
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(
            mappedBy = "profile",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Education> education = new ArrayList<>();

    @OneToMany(
            mappedBy = "profile",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Projects> projects = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_skills",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    @Builder.Default
    private Set<Skills> skills = new HashSet<>();

    @OneToMany(
            mappedBy = "profile",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Experience> experience = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_certification",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "certification_id")
    )
    @Builder.Default
    private Set<Certification> certifications = new HashSet<>();

    public void addEducation(Education edu) {
        education.add(edu);
        edu.setProfile(this);
    }

    public void removeEducation(Education edu) {
        education.remove(edu);
        edu.setProfile(null);
    }

    public void addProject(Projects project) {
        projects.add(project);
        project.setProfile(this);
    }

    public void removeProject(Projects project) {
        projects.remove(project);
        project.setProfile(null);
    }

    public void addExperience(Experience exp) {
        experience.add(exp);
        exp.setProfile(this);
    }

    public void removeExperience(Experience exp) {
        experience.remove(exp);
        exp.setProfile(null);
    }

    public void addSkill(Skills skill) {
        skills.add(skill);
        skill.getProfiles().add(this);
    }

    public void removeSkill(Skills skill) {
        skills.remove(skill);
        skill.getProfiles().remove(this);
    }

    public void addCertification(Certification certification) {
        certifications.add(certification);
        certification.getProfiles().add(this);
    }

    public void removeCertification(Certification certification) {
        certifications.remove(certification);
        certification.getProfiles().remove(this);
    }





}
