package com.donkey.security.jwt;

import com.donkey.domain.user.User;
import com.donkey.exception.JWTValidationException;
import com.fasterxml.jackson.core.JsonParser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {
    public final static long TOKEN_VALIDATION_SECOND = 1000L * 10 * 10;
    public final static long REFRESH_TOKEN_VALIDATION_SECOND = 1000L * 60 * 24 * 2;

    final static public String ACCESS_TOKEN_NAME = "accessToken";
    final static public String REFRESH_TOKEN_NAME = "refreshToken";

    @Value("${spring.jwt.secret}")
    private String SECRET_KEY;

    private Key getSigningKey(String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 유요한 토큰인지 검사한 후, 토큰에 담길 payload 값을 가져온다.
     */
    public Claims extractAllClaims(String token) throws ExpiredJwtException {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey(SECRET_KEY))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 추출한 payload 부분에서 userEmail을 가져온다.
     */
    public String getUserEmail(String token, String tokenType) throws NotFoundException {
        return extractAllClaims(token).get("userEmail" + tokenType, String.class);
    }

    /**
     * 토큰이 만료되었는지 확인한다.
     */
    public Boolean isTokenExpired(String token) {
        final Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    /**
     * AccessToken 을 생성한다.
     */
    public String generateToken(User user) {
        return doGenerateToken(ACCESS_TOKEN_NAME, user.getEmail(), TOKEN_VALIDATION_SECOND);
    }

    /**
     * RefreshToken 을 생성한다.
     */
    public String generateRefreshToken(User user) {
        return doGenerateToken(REFRESH_TOKEN_NAME, user.getEmail(), REFRESH_TOKEN_VALIDATION_SECOND);
    }

    /**
     * 토큰을 생성한다.
     * JWT 의 payload 부분에 담길 값은 "username"으로 설정한다.
     */
    public String doGenerateToken(String tokenType, String userEmail, long expireTime) {

        Claims claims = Jwts.claims();
        claims.put("userEmail" + tokenType, userEmail);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(getSigningKey(SECRET_KEY), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token, String tokenType, UserDetails userDetails) {
        try {
            final String username = getUserEmail(token, tokenType);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (NotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey(SECRET_KEY))
                    .build()
                    .parseClaimsJws(authToken);
//            Jwts.parser().setSigningKey(getSigningKey(SECRET_KEY)).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            ex.printStackTrace();
            log.error("유효하지 않은 JWT 서명");
            throw new JWTValidationException("Unavailable JWT Signature");
        } catch (MalformedJwtException ex) {
            ex.printStackTrace();
            log.error("유효하지 않은 JWT 토큰");
            throw new JWTValidationException("Unavailable JWT");
        } catch (ExpiredJwtException ex) {
            ex.printStackTrace();
            log.error("만료된 JWT 토큰");
            throw new ExpiredJwtException(null, null, "Expired JWT");
        } catch (UnsupportedJwtException ex) {
            ex.printStackTrace();
            log.error("지원하지 않는 JWT 토큰");
            throw new JWTValidationException("Unsupported JWT");
        } catch (IllegalArgumentException ex) {
            log.error("비어있는 JWT");
            throw new JWTValidationException("Empty JWT");
        }
    }
}
