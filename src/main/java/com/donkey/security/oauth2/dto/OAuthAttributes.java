package com.donkey.security.oauth2.dto;

import com.donkey.domain.user.AuthProvider;
import com.donkey.domain.user.UserType;
import com.donkey.domain.user.User;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthAttributes {

    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String profilePicture;
    private AuthProvider authProvider;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email, String profilePicture, AuthProvider authProvider) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.profilePicture = profilePicture;
        this.authProvider = authProvider;
    }

    // of()
    // OAuth2User 에서 반환하는 사용자 정보는 Map이므로 값 하나하나를 변환해야 한다.
    public static OAuthAttributes of(String registrationId, String userNameAttributeName,
                                     Map<String, Object> attributes) {

        if ("kakao".equals(registrationId))
            return ofKakao(attributes);

        if ("naver".equals(registrationId))
            return ofNaver(attributes);

        return ofGoogle(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofKakao(Map<String, Object> attributes) {
        // kakao는 kakao_account에 유저정보가 있다. (email)
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        // kakao_account안에 또 profile이라는 JSON객체가 있다. (nickname, profile_image)
        Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuthAttributes.builder()
                .name((String) kakaoProfile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .profilePicture((String) kakaoProfile.get("profile_image_url"))
                .attributes(attributes)
                .authProvider(AuthProvider.kakao)
                .nameAttributeKey("id")
                .build();
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .profilePicture((String) attributes.get("picture"))
                .attributes(attributes)
                .authProvider(AuthProvider.google)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    private static OAuthAttributes ofNaver(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .profilePicture((String) response.get("profile_image"))
                .attributes(response)
                .authProvider(AuthProvider.naver)
                .nameAttributeKey("id")
                .build();
    }

    // toEntity()
    // User 엔티티를 생성한다.
    // OAuthAttributes 에서 엔티티를 생성하는 시점 == 처음 가입할 때
    // 가입할 떄의 기본 권한을 USER로 주기 위해서 role 빌더 값에는 Role.USER를 설정한다.
    public User toEntity() {
        return User.builder()
                .name(name)
                .email(email)
                .imageUrl(profilePicture)
                .userType(UserType.UNCERTIFIED)
                .authProvider(authProvider)
                .build();
    }

    @Override
    public String toString() {
        return "OAuthAttributes{" +
                "attributes=" + attributes +
                ", nameAttributeKey='" + nameAttributeKey + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", profilePicture='" + profilePicture + '\'' +
                '}';
    }
}
