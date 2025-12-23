package com.example.user_service.dto.responseDto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDto
{
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String name;
    private String avatarUrl;

    public AuthResponseDto(String name, String email)
    {
        this.name=name;
        this.email=email;
    }

    public AuthResponseDto(Long id, String token, String email, String name)
    {
        this.id=id;
        this.token=token;
        this.email=email;
        this.name=name;
    }

    public AuthResponseDto(Long id, String name, String email)
    {
        this.id=id;
        this.name=name;
        this.email=email;
    }
}
