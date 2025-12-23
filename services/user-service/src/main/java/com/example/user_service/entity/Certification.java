package com.example.user_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "certification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true,of = "name")
public class Certification extends BaseModel{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String issuedBy;
    private Integer year;

    @ManyToMany(mappedBy = "certifications", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<UserProfile> profiles = new HashSet<>();
}
