package com.donkey.domain.user;

import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable
@Getter
public class UniversityInfo {

    private String studentNumber;
    private String studentEmail;

    public UniversityInfo() {}

    public UniversityInfo(String studentNumber, String studentEmail) {
        this.studentNumber = studentNumber;
        this.studentEmail = studentEmail;
    }
}
