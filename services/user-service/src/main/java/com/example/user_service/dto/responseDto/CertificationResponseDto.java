package com.example.user_service.dto.responseDto;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificationResponseDto
{
    private Long id;
    private String name;
    private String issuedBy;
    private Integer year;
    private Date createdAt;
    private Date updatedAt;
}
