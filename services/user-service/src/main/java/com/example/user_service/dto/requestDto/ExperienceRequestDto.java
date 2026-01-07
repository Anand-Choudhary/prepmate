package com.example.user_service.dto.requestDto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceRequestDto
{
    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Role is required")
    private String role;

    @NotNull(message = "Start year is required")
    @Min(value = 1900, message = "Start year must be after 1900")
    @Max(value = 2100, message = "Start year must be before 2100")
    private Integer startYear;

    @NotNull(message = "Currently working status is required")
    private Boolean currentlyWorking;

    @Min(value = 1900, message = "End year must be after 1900")
    @Max(value = 2100, message = "End year must be before 2100")
    private Integer endYear;

    @AssertTrue(message = "endYear must be null for current job and present for past job")
    private boolean isEndYearValid() {
        if (Boolean.TRUE.equals(currentlyWorking)) {
            return endYear == null;
        }
        return endYear != null;
    }
}
