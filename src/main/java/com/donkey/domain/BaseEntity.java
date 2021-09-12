package com.donkey.domain;

import com.donkey.domain.enums.Status;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime modifiedDate;

    @Enumerated(EnumType.STRING)
    private Status status = Status.NORMAL;

    @PreUpdate
    public void onModifiedDateUpdate() {
        modifiedDate = LocalDateTime.now();
    }

    public void softDelete() {
        this.status = Status.DELETE;
    }

    public void rollback() {
        this.status = Status.NORMAL;
    }

}