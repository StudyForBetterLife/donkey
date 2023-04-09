package com.donkey.domain.user;

import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable
@Getter
public class Address {

    private String si;
    private String gu;
    private String dong;
    private String detail;

    public Address() {
    }

    public Address(String si, String gu, String dong, String detail) {
        this.si = si;
        this.gu = gu;
        this.dong = dong;
        this.detail = detail;
    }

    public String toWholeAddress() {
        return si + " " + gu + " " + dong + " " + detail;
    }
}
