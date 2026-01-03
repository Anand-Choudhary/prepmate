package com.interview_platform.payment_service.entity;

import jakarta.persistence.*;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;

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

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
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
