package com.donkey.security.oauth2;

import com.donkey.domain.user.AuthProvider;
import com.donkey.exception.OAuth2AuthenticationProcessingException;
import com.donkey.security.UserPrincipal;
import com.donkey.security.oauth2.dto.OAuthAttributes;
import com.donkey.security.oauth2.dto.SessionUser;
import com.donkey.domain.user.User;
import com.donkey.repository.UserRepository;
import com.donkey.security.oauth2.user.OAuth2UserInfo;
import com.donkey.security.oauth2.user.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequestEntityConverter;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final HttpSession httpSession;
    private RestOperations restOperations;
    private final Converter<OAuth2UserRequest, RequestEntity<?>> requestEntityConverter =
            new OAuth2UserRequestEntityConverter();
    private static final ParameterizedTypeReference<Map<String, Object>> PARAMETERIZED_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        this.restOperations = restTemplate;


        // registrationId
        // 햔재 로그인 진행 중인 서비스를 구분하는 코드
        // 네이버 로그인인지, 구글 로그인인지 구분하기위해 사용한다.
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // userNameAttributeName
        // OAuth2 로그인 진행 시 키가 되는 필드값을 말한다. PK와 같은 의미이다.
        // 구글의 경우 기본적으로 코드를 지원한다. 구글의 기본 코드는 "sub"이다. (네이버, 카카오 등은 기본 지원하지 않는다.)
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();

//        OAuthAttributes attributes = getOAuthAttributes(userRequest, delegate, registrationId, userNameAttributeName);
//
//        // SessionUser
//        // 세션에 사용자 정보를 저장하기 위한 Dto 클래스이다.
//        User user = saveOrUpdate(attributes);
//        httpSession.setAttribute("user", new SessionUser(user));
//
//        return new DefaultOAuth2User(
//                Collections.singleton(new SimpleGrantedAuthority(user.getUserTypeKey())),
//                attributes.getAttributes(),
//                attributes.getNameAttributeKey()
//        );

        try {
            return processOAuth2User(userRequest, delegate, registrationId);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuthAttributes getOAuthAttributes(
            OAuth2UserRequest userRequest,
            OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate,
            String registrationId,
            String userNameAttributeName) {

        // OAuthAttributes
        // OAuth2UserService를 통해 가져온 OAuth2User의 attributes를 담을 클래스이다.
        // 다른 소셜 로그인도 이 클래스를 사용한다.
        Map<String, Object> userAttributes;


        if (registrationId.equals(AuthProvider.google.name())) {
            // google 의 경우 기본 DefaultOAuth2UserService 클래스의 loadUser 메소드로 userAttributes 를 얻을 수 있다.
            OAuth2User oAuth2User = delegate.loadUser(userRequest);
            userAttributes = oAuth2User.getAttributes();
        } else {
            // naver, kakao 의 경우 getCustomAttributes 으로 userAttributes 를 파싱한다.
            userAttributes = getAttributesForKakao(userRequest);
        }
        return OAuthAttributes.of(registrationId, userNameAttributeName, userAttributes);
    }

    private User saveOrUpdate(OAuthAttributes attributes) {
        User user = userRepository.findByEmail(attributes.getEmail())
                .map(entity -> entity.update(
                        User.builder()
                                .name(attributes.getName())
                                .imageUrl(attributes.getProfilePicture())
                                .authProvider(attributes.getAuthProvider())
                                .build()
                ))
                .orElse(attributes.toEntity());

        return userRepository.save(user);
    }


    private Map<String, Object> getAttributesForKakao(OAuth2UserRequest userRequest) {
        RequestEntity<?> request = this.requestEntityConverter.convert(userRequest);
        assert request != null;
        ResponseEntity<Map<String, Object>> response = this.restOperations.exchange(request, PARAMETERIZED_RESPONSE_TYPE);
        Map<String, Object> userAttributes = response.getBody();
        assert userAttributes != null;

        if (userAttributes.containsKey("response")) {
            LinkedHashMap responseData = (LinkedHashMap) userAttributes.get("response");
            userAttributes.putAll(responseData);
            userAttributes.remove("response");
        }
        return userAttributes;
    }


    // 시용자 정보 추출
    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest,
                                         OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate,
                                         String registrationId) {

        Map<String, Object> userAttributes;
        if (registrationId.equals(AuthProvider.google.name())) {
            // google 의 경우 기본 DefaultOAuth2UserService 클래스의 loadUser 메소드로 userAttributes 를 얻을 수 있다.
            OAuth2User oAuth2User = delegate.loadUser(oAuth2UserRequest);
            userAttributes = oAuth2User.getAttributes();
        } else {
            // naver, kakao 의 경우 getCustomAttributes 으로 userAttributes 를 파싱한다.
            userAttributes = getAttributesForKakao(oAuth2UserRequest);
        }

        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, userAttributes);
        if (StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("OAuth2 공급자(구글, 네이버, ...) 에서 이메일을 찾을 수 없습니다.");
        }

        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (!user.getAuthProvider().equals(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
                throw new OAuth2AuthenticationProcessingException(
                        user.getAuthProvider() + "계정을 사용하기 위해서 로그인을 해야합니다.");
            }
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        httpSession.setAttribute("user", new SessionUser(user));
        return UserPrincipal.create(user, userAttributes);
    }

    // DB에 존재하지 않을 경우 새로 등록
    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {

        return userRepository.save(User.builder()
                .name(oAuth2UserInfo.getName())
                .email(oAuth2UserInfo.getEmail())
                .imageUrl(oAuth2UserInfo.getImageUrl())
                .authProvider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))
                .usrId(oAuth2UserInfo.getId())
                .build()
        );
    }

    // DB에 존재할 경우 정보 업데이트
    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        return userRepository.save(existingUser
                .update(
                        User.builder()
                                .name(oAuth2UserInfo.getName())
                                .imageUrl(oAuth2UserInfo.getImageUrl())
                                .build()
                )
        );
    }
}
