package com.example.user_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
    private Date createdAt;

    @Column(
            name = "updated_at"
    )
    private Date updatedAt;

    public BaseModel() {
    }


    @PrePersist
    void beforeCreate() {
        if (this.createdAt == null) {
            this.setCreatedAt(new Date());
        }
        this.setUpdatedAt(new Date());
    }

    @PreUpdate
    void beforeUpdate() {
        this.setUpdatedAt(new Date());
    }

}

