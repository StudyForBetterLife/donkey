package com.donkey.security.oauth2.user;

import com.donkey.domain.user.AuthProvider;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase(AuthProvider.kakao.name())) {
            return new KakaoOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(AuthProvider.apple.name())) {
            return new AppleOAuth2UserInfo(attributes);
        } else {
            throw new RuntimeException(registrationId + " 로그인은 지원하지 않습니다.");
        }
    }
}
