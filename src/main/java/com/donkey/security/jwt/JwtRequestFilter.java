package com.donkey.security.jwt;

import com.donkey.api.dto.BaseRes;
import com.donkey.api.dto.JWTResponseRes;
import com.donkey.domain.user.User;
import com.donkey.exception.JWTValidationException;
import com.donkey.security.CustomUserDetailsService;
import com.donkey.security.TokenProvider;
import com.donkey.util.redis.RedisUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * [Spring Security 간단 정리]
 * SecurityContextPersistenceFilter : SecurityContext 객체를 로딩하여 SecurityContextHolder에 저장하고 요청이 끝나면 삭제
 * LogoutFilter : 지정한 경로의 요청이 들어오면 사용자를 로그아웃시킴
 * UsernamePasswordAuthennticationFilter : 로그인 요청이 들어오면 아이디/비밀번호 기반의 인증을 수행한다.
 * FilterSecurityInterceptor : 인증에 성공한 사용자가 해당 리소스에 접근할 권한이 있는지를 검증
 * <p>
 * [JwtRequestFilter]
 * UsernamePasswordAuthenticationFilter 앞에 Custom Filter를 두어
 * 세션이 존재하지 않아도 올바른 Jwt 값이 존재하면,
 * SecurityContextHolder에 UserDetail 정보를 넣어 로그인 된 사용자로 인식 하도록 할 것이다
 */
@Slf4j
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CookieService cookieService;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.contains("/api/auth/") ||
                path.matches(".*/v2/api-docs*.") ||
                path.contains("swagger");
    }

    /**
     * 1. 로그인 한 사용자는 AccessToken과 RefreshToken을 가지고 있다.
     * 2. AccessToken이 유효하면 AccessToken내 payload를 읽어 사용자와 관련있는 UserDetail을 생성
     * 3. AccessToken이 유효하지 않으면 RefreshToken값을 읽어드림.
     * 4. RefreshToken을 읽어 AccessToken을 사용자에게 재생성하고, 요청을 허가시킴.
     * <p>
     * - 발행된 AccessToken의 값은 무조건적으로 명백하다고 생각하여 요청을 허가시킴.
     * But Access Token탈취의 위험이 존재하기 때문에 짧은 유효시간을 두어,
     * Access Token이 탈취 당하더라도 만료되어 사용할 수 없도록 한다.
     * <p>
     * - Refresh Token은 서버에서 그 값(Redis)을 저장함.
     * Refresh Token을 사용할 상황이 오면 반드시 서버에서 그 유효성을 판별,
     * 유효하지 않는 경우라면 요청을 거부.
     * 혹은 사용자로부터 탈취 됐다라는 정보가 오면 그 Refrsh Token을 폐기할 수 있도록 설정.
     * <p>
     * - Redis를 사용하는 이유
     * Refresh Token은 만료되어야 하기 때문이다
     * 그래서 휘발성을 가진 데이터베이스인 Redis를 사용한다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        /*
         * new flow (자체 로그인)
         */

        //로그인 한 사용자는 AccessToken 과 RefreshToken 을 가지고 있다.
        //final Cookie jwtToken = cookieService.getCookie(request, JwtUtil.ACCESS_TOKEN_NAME);
        String jwtToken = getJwtFromRequest(request);

        String jwt = null;
        String userEmail = null;
        String refreshJwt = null;
        String refreshUserEmail = null;

        try {
            if (jwtUtil.validateToken(jwtToken)) {
                jwt = jwtToken;
                userEmail = jwtUtil.getUserEmail(jwt, JwtUtil.ACCESS_TOKEN_NAME);
                log.error("jwt = " + jwt);
                log.error("userEmail = " + userEmail);
            }
            if (userEmail != null) {
                //AccessToken이 유효하면 AccessToken내 payload를 읽어 사용자와 관련있는 UserDetail을 생성
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);

                if (jwtUtil.validateToken(jwt, JwtUtil.ACCESS_TOKEN_NAME, userDetails)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // SecurityContextHolder : Spring Security 의 인메모리 세션저장소
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    log.error("usernamePasswordAuthenticationToken : " + usernamePasswordAuthenticationToken.getDetails());
                }
            }
        } catch (ExpiredJwtException e) {
            e.printStackTrace();
            response.setContentType("application/json");
            response.getWriter().write(convertObjectToJson(
                    JWTResponseRes.builder()
                            .success(false)
                            .expired(true)
                            .message(e.getMessage())
                            .build()
            ));
            return;

//            String refreshTokenFromHeader = request.getHeader(JwtUtil.REFRESH_TOKEN_NAME);
//            Cookie refreshToken = cookieService.getCookie(request, JwtUtil.REFRESH_TOKEN_NAME);
//            if (refreshToken != null) {
//                refreshJwt = refreshToken.getValue();
//            }
        } catch (NotFoundException | JWTValidationException e) {
            response.setContentType("application/json");
            response.getWriter().write(convertObjectToJson(
                    JWTResponseRes.builder()
                            .success(false)
                            .expired(false)
                            .message(e.getMessage())
                            .build()
            ));
            return;
        }

        try {
            if (refreshJwt != null) {
                // AccessToken이 유효하지 않으면 RefreshToken 값을 읽는다.
                refreshUserEmail = redisUtil.getData(refreshJwt);
                log.error("refreshUserEmail = " + refreshUserEmail);

                if (refreshUserEmail.equals(jwtUtil.getUserEmail(refreshJwt, JwtUtil.REFRESH_TOKEN_NAME))) {
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(refreshUserEmail);
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

                    User user = User.builder()
                            .name(refreshUserEmail)
                            .build();
                    String newToken = jwtUtil.generateToken(user);

                    // RefreshToken을 읽어 AccessToken을 사용자에게 재생성하고 (헤더속 쿠키로 전달), 요청을 허가시킴.
                    // Cookie: attribute1=value1; attribute2=value2;
                    Cookie newAccessToken = cookieService.createCookie(JwtUtil.ACCESS_TOKEN_NAME, newToken);
                    response.addHeader("Cookie", String.valueOf(newAccessToken));
                    response.addCookie(newAccessToken);
                }
            }
        } catch (ExpiredJwtException | NotFoundException e) {
            response.setContentType("application/json");
            response.getWriter().write(convertObjectToJson(
                    JWTResponseRes.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            ));
            return;
        }

        filterChain.doFilter(request, response);

        /*
         * old flow (Oauth 로그인)
         *
        try {
            String jwt2 = getJwtFromRequest(request);

            if (tokenProvider.validateToken(jwt2)) {
                logger.error("JWT = " + jwt2);
                Long userId = tokenProvider.getUserIdFromToken(jwt2);

                UserDetails userDetails = customUserDetailsService.loadUserById(userId);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // SecurityContextHolder : Spring Security 의 인메모리 세션저장소
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Security Context에서 사용자 인증을 설정할 수 없습니다", ex);
        }

        filterChain.doFilter(request, response);
         */

    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (Objects.isNull(bearerToken)) {
            return null;
        }
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private String convertObjectToJson(Object object) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(object);
    }
}