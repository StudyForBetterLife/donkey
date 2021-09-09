package com.donkey.domain;

import com.donkey.domain.enums.Status;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass // 엔티티의 상위 클래스가 된다
@EntityListeners(AuditingEntityListener.class) // Auditing 기능 포함
public abstract class BaseEntity {

    @CreatedDate // 엔티티가 생성되어 저장될 떄의 시간이 자동 저장
    private LocalDateTime createdDate;

    @LastModifiedDate // 조회한 엔티티의 값을 변경할 떄의 시간이 자동 저장
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