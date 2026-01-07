package com.example.user_service.dto.responseDto;


import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillResponseDto
{
    private Long id;
    private String name;
    private Date createdAt;
    private Date updatedAt;
}
