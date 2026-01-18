package com.interview_platform.payment_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;

@Setter
@Getter
@MappedSuperclass
@ToString
public class BaseModel
{
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(
            name = "id"
    )
    @Id
    private Long id;

    @Column(
            name = "created_at"
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at"
    )
    private LocalDateTime updatedAt;



    public BaseModel() {
    }


    @PrePersist
    void beforeCreate() {
        if (this.createdAt == null) {
            this.setCreatedAt(LocalDateTime.now());
        }
        this.setUpdatedAt(LocalDateTime.now());
    }

    @PreUpdate
    void beforeUpdate() {
        this.setUpdatedAt(LocalDateTime.now());
    }

}
