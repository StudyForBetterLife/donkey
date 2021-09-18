package com.donkey.security.oauth2.user;

import java.util.Map;

public class KakaoOAuth2UserInfo extends OAuth2UserInfo {

    Map<String, Object> kakaoAccount;
    // kakao_account안에 또 profile이라는 JSON객체가 있다. (nickname, profile_image)
    Map<String, Object> kakaoProfile;

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
        kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");
    }

    @Override
    public String getId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getName() {
        return String.valueOf(kakaoProfile.get("nickname")) ;
    }

    @Override
    public String getEmail() {
        return String.valueOf(kakaoAccount.get("email"));
    }

    @Override
    public String getImageUrl() {
        return String.valueOf(kakaoProfile.get("profile_image_url")) ;
    }
}
