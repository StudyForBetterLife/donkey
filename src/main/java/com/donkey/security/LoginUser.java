package com.donkey.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

/**
 * @Target - 해당 어노테이션이 생성될 수 있는 위치를 지정한다. PARAMETER로 지정했으므로
 * 메소드의 파라미터로 선언된 객체에서만 사용가능하다.
 * @Retention - 어노테이션 정보 유지에 대한 설정을 할 수 있다.
 * @Document - 해당 어노테이션이 지정된 대상의 JavaDoc에 이 어노테이션의 존재를 표기
 * @AuthenticationPrincipal - 로그인한 사용자의 정보를 파라미터로 받을 때 사용
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
// @interface
// 이 파일을 어노테이션 클래스로 지정한다.
public @interface LoginUser {
}
