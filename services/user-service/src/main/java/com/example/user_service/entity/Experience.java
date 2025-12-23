package com.example.user_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;

@Entity
@Table(name = "experience")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Experience extends BaseModel{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String role;

    @Column(name = "start_year", nullable = false)
    private Integer startYear;

    @Column(name = "currently_working", nullable = false)
    private Boolean currentlyWorking;

    @Column(name = "end_year")
    private Integer endYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private UserProfile profile;

    /**
     * Business rule:
     * - If currentlyWorking = true  → endYear must be null
     * - If currentlyWorking = false → endYear must be present
     */
    @AssertTrue(message = "endYear must be null for current job and present for past job")
    private boolean isEndYearValid() {
        if (Boolean.TRUE.equals(currentlyWorking)) {
            return endYear == null;
        }
        return endYear != null;
    }
}
