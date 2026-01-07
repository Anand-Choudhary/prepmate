package com.example.user_service.dto.responseDto;

import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCompleteResponseDto
{
    private Long id;
    private String name;
    private String email;
    private LocalDate dob;
    private String mobile;
    private String profileImg;
    private String baseImg;
    private UserProfileResponseDto profile;
    private Date createdAt;
    private Date updatedAt;
}
