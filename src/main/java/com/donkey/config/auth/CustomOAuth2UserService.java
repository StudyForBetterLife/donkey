package com.donkey.config.auth;

import com.donkey.config.auth.dto.OAuthAttributes;
import com.donkey.config.auth.dto.SessionUser;
import com.donkey.domain.enums.AuthProvider;
import com.donkey.domain.user.User;
import com.donkey.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequestEntityConverter;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final UserRepository userRepository;
    private final HttpSession httpSession;
    private RestOperations restOperations;
    private final Converter<OAuth2UserRequest, RequestEntity<?>> requestEntityConverter = new OAuth2UserRequestEntityConverter();
    private static final ParameterizedTypeReference<Map<String, Object>> PARAMETERIZED_RESPONSE_TYPE =
            new ParameterizedTypeReference<Map<String, Object>>() {
            };

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("=== OAuth2User loadUser ===");
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        this.restOperations = restTemplate;

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        System.out.println("=== userRequest = " + userRequest.toString());


        // registrationId
        // 햔재 로그인 진행 중인 서비스를 구분하는 코드
        // 네이버 로그인인지, 구글 로그인인지 구분하기위해 사용한다.
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        System.out.println("=== registrationId = " + registrationId);

        // userNameAttributeName
        // OAuth2 로그인 진행 시 키가 되는 필드값을 말한다. PK와 같은 의미이다.
        // 구글의 경우 기본적으로 코드를 지원한다. 구글의 기본 코드는 "sub"이다. (네이버, 카카오 등은 기본 지원하지 않는다.)
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        System.out.println("=== userNameAttributeName = " + userNameAttributeName);

        // OAuthAttributes
        // OAuth2UserService를 통해 가져온 OAuth2User의 attributes를 담을 클래스이다.
        // 다른 소셜 로그인도 이 클래스를 사용한다.
        Map<String, Object> userAttributes;
        if (registrationId.equals("google")) {
            // google 의 경우 기본 DefaultOAuth2UserService 클래스의 loadUser 메소드로 userAttributes 를 얻을 수 있다.
            OAuth2User oAuth2User = delegate.loadUser(userRequest);
            userAttributes = oAuth2User.getAttributes();
        } else {
            // naver, kakao 의 경우 getCustomAttributes 으로 userAttributes 를 파싱한다.
            RequestEntity<?> request = this.requestEntityConverter.convert(userRequest);
            assert request != null;
            ResponseEntity<Map<String, Object>> response = this.restOperations.exchange(request, PARAMETERIZED_RESPONSE_TYPE);
            userAttributes = getCustomAttributes(response);
        }
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, userAttributes);

        System.out.println("=== attributes.toString() = " + attributes.toString());

        // SessionUser
        // 세션에 사용자 정보를 저장하기 위한 Dto 클래스이다.
        User user = saveOrUpdate(attributes);
        httpSession.setAttribute("user", new SessionUser(user));

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getUserTypeKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey()
        );
    }

    private User saveOrUpdate(OAuthAttributes attributes) {
        User user = userRepository.findByEmail(attributes.getEmail())
                .map(entity -> entity.update(
                        User.builder()
                                .name(attributes.getName())
                                .profilePicture(attributes.getProfilePicture())
                                .authProvider(attributes.getAuthProvider())
                                .build()
                ))
                .orElse(attributes.toEntity());

        return userRepository.save(user);
    }

    private Map<String, Object> getCustomAttributes(ResponseEntity<Map<String, Object>> response) {
        Map<String, Object> userAttributes = response.getBody();
        assert userAttributes != null;
        if (userAttributes.containsKey("response")) {
            LinkedHashMap responseData = (LinkedHashMap) userAttributes.get("response");
            userAttributes.putAll(responseData);
            userAttributes.remove("response");
        }
        return userAttributes;
    }

    private AuthProvider getProvider(String registrationId) {
        AuthProvider provider;
        switch (registrationId) {
            case "kakao":
                provider= AuthProvider.KAKAO;
                break;
            case "naver":
                provider= AuthProvider.NAVER;
                break;
            case "apple":
                provider= AuthProvider.APPLE;
                break;
            default:
                provider = AuthProvider.LOCAL;
                break;
        }

        return provider;
    }
}
