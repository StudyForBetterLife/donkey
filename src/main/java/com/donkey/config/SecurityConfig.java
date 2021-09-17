package com.donkey.config;

import com.donkey.security.CustomUserDetailsService;
import com.donkey.security.TokenAuthenticationFilter;
import com.donkey.security.oauth2.*;
import com.donkey.domain.user.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
// @EnableWebSecurity
// spring security 설정들을 활성화시켜준다.
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter();
    }

    /*
      By default, Spring OAuth2 uses HttpSessionOAuth2AuthorizationRequestRepository to save
      the authorization request. But, since our service is stateless, we can't save it in
      the session. We'll save the request in a Base64 encoded cookie instead.

      HttpCookieOAuth2AuthorizationReqeustRepository
      - JWT를 사용하기 때문에 Session에 저장할 필요가 없어져, Authorization Request를 Based64 encoded cookie에 저장
    */

    @Bean
    public HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    // Authorization에 사용할 userDetailService와 password Encoder 정의
    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    // SecurityConfig에서 사용할 password encoder를 BCryptPasswordEncoder로 정의
    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    /*
      AuthenticationManager를 외부에서 사용하기 위해 AuthenticationManager Bean을 통해
      @Autowired가 아닌 @Bean 설정으로 Spring Security 밖으로 Authentication 추출
     */
    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
    // .csrf().disable().headers().frameOptions().disable()
    // h2-console 화면을 사용하기 위해서 해당 옵션들을 disable 한다.
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                .csrf().disable()
                .headers().frameOptions().disable()

                .and()
                // authorizeRequests
                // URL별 권한 관리를 설정하는 옵션의 시작점이다. authorizeRequests가 선언되어야만 antMatchers 옵션을 사용할 수 있다.
                .authorizeRequests()
                // antMatchers
                // 권한 관리 대상을 지정하는 옵션이다.
                // URL, HTTP 메소드별로 관리가 가능하다. 지정된 URL들은 permitAll() 옵션을 통해 전체 열람 관한을 준다.
                .antMatchers("/").permitAll()
                .antMatchers("/api/**").hasAnyRole(UserType.CERTIFIED.getValue(), UserType.UNCERTIFIED.getValue())
                .antMatchers("/socket-endpoint/**").permitAll()
                .antMatchers("/oauth/**", "/oauth2/**", "/login/**").permitAll()
                .antMatchers("/uploads/**", "/css/**", "/js/**", "/h2-console?**").permitAll()
                .anyRequest().authenticated()

                .and()
                // .logout().logoutSuccessUrl("/")
                // 로그아웃 기능에 대한 여러 설정의 진입점이다. 로그아웃 성공시 "/" 주소로 이동한다.
                .logout()
                .logoutSuccessUrl("/")
                .and()
                // oauth2Login
                // OAuth 2 로그인 기능에 대한 여러 설정의 진입점이다.
                // userInfoEndpoint
                // OAuth 2 로그인 성공 이후 사용자 정보를 가져올 떄의 설정들을 담당한다.
                .oauth2Login()
                .authorizationEndpoint()
                .baseUri("/oauth2/authorization")
                .authorizationRequestRepository(cookieAuthorizationRequestRepository())

                .and()
                .userInfoEndpoint()
                // userService
                // 소셜 로그인 성공시 후속 조치를 진행할 UserService 인터페이스의 구현체를 등록한다.
                // 리소스 서버(소셜 서비스들)에서 사용자 정보를 가져온 상태에서 추가로 진행하고자하는 기능을 명시할 수 있다.
                .userService(customOAuth2UserService)

                .and()
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler);

        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}