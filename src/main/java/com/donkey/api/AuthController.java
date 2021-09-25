package com.donkey.api;

import com.donkey.api.dto.*;
import com.donkey.domain.user.AuthProvider;
import com.donkey.domain.user.User;
import com.donkey.exception.JWTValidationException;
import com.donkey.security.jwt.CookieService;
import com.donkey.security.jwt.JwtUtil;
import com.donkey.util.redis.RedisUtil;
import com.donkey.service.AuthService;
import com.donkey.util.RandomNumberGenerator;
import com.donkey.util.encrypt.EncryptHelper;
import com.donkey.util.mail.MailDto;
import com.donkey.util.mail.MailService;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EncryptHelper encryptHelper;
    private final MailService mailService;
    private final JwtUtil jwtUtil;
    private final CookieService cookieService;
    private final RedisUtil redisUtil;


    @GetMapping("/register/{email}")
    @ApiOperation(value = "이메일 중복확인")
    public BaseRes checkEmail(@PathVariable("email") String email) {
        boolean exists = authService.existsByEmail(email);
        String message = exists ? "이미 존재하는 이메일입니다." : "사용 가능한 이메일입니다.";
        return BaseRes.builder()
                .success(!exists)
                .message(message)
                .build();
    }

    @PostMapping("/register")
    public CreateUserRes register(@RequestBody CreateUserReq req) {
        if (!StringUtils.hasText(req.getEmail()) ||
                !StringUtils.hasText(req.getName()) ||
                !StringUtils.hasText(req.getPassword()) ||
                !StringUtils.hasText(req.getTelNum())) {
            return CreateUserRes.builder()
                    .success(false)
                    .message("필드를 모두 채워주세요.")
                    .build();
        }

        if (authService.existsByEmail(req.getEmail())) {
            return CreateUserRes.builder()
                    .success(false)
                    .message("이미 존재하는 유저입니다")
                    .build();
        }

        User user = User.builder()
                .email(req.getEmail())
                .name(req.getName())
                .usrId(req.getUserId())
                .nickName(req.getNickName())
                .authProvider(AuthProvider.none)
                .password(encryptHelper.encrypt(req.getPassword()))
                .telNum(req.getTelNum())
                .build();
        try {
            Long id = authService.save(user);
            sendVerificationMail(user);
            return CreateUserRes.builder()
                    .success(true)
                    .message("회원 가입에 성공했습니다. 인증메일을 확인하세요.")
                    .id(id)
                    .build();
        } catch (Exception e) {
            return CreateUserRes.builder()
                    .success(false)
                    .message("회원가입을 하는 도중 오류가 발생했습니다.")
                    .build();
        }
    }

    @GetMapping("/verify/{key}")
    public BaseRes emailVerify(@PathVariable("key") String key) {
        try {
            verifyEmail(key);
            return BaseRes.builder()
                    .success(true)
                    .message("성공적으로 인증메일을 확인했습니다.")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return BaseRes.builder()
                    .success(false)
                    .message("인증메일을 확인하는데 실패했습니다.")
                    .build();
        }
    }

    @PostMapping("/login")
    public LoginRes login(@RequestBody LoginUserReq req,
                          HttpServletRequest servletRequest,
                          HttpServletResponse servletResponse) {
        Optional<User> optionalUser = authService.findByEmail(req.getEmail());
        if (optionalUser.isEmpty()) {
            return LoginRes.builder()
                    .success(false)
                    .message("존재하지 않는 이메일입니다.")
                    .build();
        }

        User user = optionalUser.get();
        boolean isCorrect = encryptHelper.isMatch(req.getPassword(), user.getPassword());
        if (!isCorrect) {
            return LoginRes.builder()
                    .success(false)
                    .message("잘못된 비밀번호 입니다.")
                    .build();
        }

        if (user.getAuthProvider().equals(AuthProvider.none)) {
            return LoginRes.builder()
                    .success(false)
                    .message("회원가입 인증메일을 확인해주세요")
                    .build();
        }
        /*
        user의 id,pw가 맞으면 토큰과 refresh token을 쿠키값으로 전달한다. -> 일단 response로 전달
         */
        try {
            final String accessJwt = jwtUtil.generateToken(user);
            final String refreshJwt = jwtUtil.generateRefreshToken(user);
            // refresh token은 redis에 저장한다.
            redisUtil.setDataExpire(refreshJwt, user.getEmail(), JwtUtil.REFRESH_TOKEN_VALIDATION_SECOND);
            servletResponse.addHeader(JwtUtil.ACCESS_TOKEN_NAME, accessJwt);
            servletResponse.addHeader(JwtUtil.REFRESH_TOKEN_NAME, refreshJwt);

//            Cookie accessToken = cookieService.createCookie(JwtUtil.ACCESS_TOKEN_NAME, accessJwt);
//            Cookie refreshToken = cookieService.createCookie(JwtUtil.REFRESH_TOKEN_NAME, refreshJwt);
//            servletResponse.addCookie(accessToken);
//            servletResponse.addCookie(refreshToken);

            return LoginRes.builder()
                    .success(true)
                    .message("'" + user.getName() + "' 님 환영합니다.")
                    .accessToken(accessJwt)
                    .refreshToken(refreshJwt)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return LoginRes.builder()
                    .success(false)
                    .message("로그인에 실패했습니다. " + e.getMessage())
                    .build();

        }
    }

    @PostMapping("/reissue")
    @ApiOperation(value = "Access Token 재발급")
    public TokenReissueRes reissueToken(@RequestBody TokenReissueReq tokenReissueReq,
                                        HttpServletResponse servletResponse,
                                        HttpServletRequest servletRequest) {
        Optional<User> optionalUser = authService.findByEmail(tokenReissueReq.getEmail());
        if (optionalUser.isEmpty()) {
            return TokenReissueRes.builder()
                    .success(false)
                    .message("존재하지 않는 유저입니다.")
                    .build();
        }

        try {
            if (jwtUtil.validateToken(tokenReissueReq.getAccessToken())) {
                return TokenReissueRes.builder()
                        .success(false)
                        .message("Access Token 이 아직 만료되지 않았습니다.")
                        .build();
            }
        } catch (ExpiredJwtException e) {
            User user = optionalUser.get();
            // redis 에서 refreshToken=email 형식으로 저장되어 있다.
            String userEmail = redisUtil.getData(tokenReissueReq.getRefreshToken());
            if (!StringUtils.hasText(userEmail) || !userEmail.equals(user.getEmail())) {
                return TokenReissueRes.builder()
                        .success(false)
                        .message("유저에게 발급된 Refresh Token 이 아닙니다.")
                        .build();
            }
            String newAccessToken = jwtUtil.generateToken(user);
            servletResponse.addHeader(JwtUtil.ACCESS_TOKEN_NAME, newAccessToken);
            return TokenReissueRes.builder()
                    .success(true)
                    .message("새로운 Access Token 발급을 완료했습니다.")
                    .accessToken(newAccessToken)
                    .build();
        } catch (JWTValidationException e) {
            return TokenReissueRes.builder()
                    .success(false)
                    .message("새로운 Access Token 발급을 실패했습니다.")
                    .build();
        }

        return TokenReissueRes.builder()
                .success(false)
                .message("새로운 Access Token 발급을 실패했습니다.")
                .build();
    }

    @PostMapping("/find-email")
    public BaseRes findUserEmail(@RequestBody FindUserEmailReq req) {
        Optional<User> optionalUser = authService.findUserByUserIdAndTelNum(req.getUserId(), req.getTelNum());
        if (optionalUser.isEmpty()) {
            return BaseRes.builder()
                    .success(false)
                    .message("존재하지 않는 유저입니다.")
                    .build();
        }
        User user = optionalUser.get();
        return BaseRes.builder()
                .success(true)
                .message(user.getEmail())
                .build();
    }

    @PostMapping("/find-password")
    public BaseRes findUserPassword(@RequestBody FindUserPasswordReq req) {
        Optional<User> optionalUser = authService.findUserByEmailAndUserIdAndTelNum(req.getEmail(), req.getUserId(), req.getTelNum());
        if (optionalUser.isEmpty()) {
            return BaseRes.builder()
                    .success(false)
                    .message("존재하지 않는 유저입니다.")
                    .build();
        }

        // 기존 패스워드 변경 로직
        User user = optionalUser.get();
        String randomStr = new RandomNumberGenerator().generateTokenForPassword();
        String encrypted = encryptHelper.encrypt(randomStr);
        authService.updateTemporaryPassword(user.getId(), encrypted);

        // 유저 메일로 변경된 패스워드 보내기
        sendMail(user.getEmail(), randomStr);

        return BaseRes.builder()
                .success(true)
                .message(user.getEmail() + " 로 임시 패스워드 전송")
                .build();
    }

    private void sendMail(String to, String message) {
        new Thread(() -> {
            try {
                mailService.sendEmailForPassword(MailDto.builder()
                        .to(to)
                        .subject("[당나귀] 임시 패스워드입니다.")
                        .token(message)
                        .build());
                log.error("메일 보내기 성공 : 변경된 패스워드");
            } catch (MessagingException e) {
                log.error("메일 보내기 실패 : 변경된 패스워드");
                e.printStackTrace();
            }
        }).start();
    }

    private void sendVerificationMail(User user) {
        new Thread(() -> {
            try {
                final String VERIFICATION_LINK = "http://localhost:8081/api/users/verify/";
                UUID uuid = UUID.randomUUID();
                // key = uuid, value = email
                redisUtil.setDataExpire(uuid.toString(), user.getEmail(), 60 * 10L);
                mailService.sendEmailForEmailVerification(MailDto.builder()
                        .to(user.getEmail())
                        .subject("[당나귀] 회원가입 인증메일입니다.")
                        .userName(user.getName())
                        .token(VERIFICATION_LINK + uuid)
                        .build());
                log.error("메일 보내기 성공 : 이메일 인증");
            } catch (MessagingException e) {
                log.error("메일 보내기 실패 : 이메일 인증");
                e.printStackTrace();
            }

        }).start();
    }

    private void verifyEmail(String key) {
        // redis 에서 key (uuid)에 대한 value (email)를 꺼낸다.
        String userEmail = redisUtil.getData(key);
        Optional<User> optionalUser = authService.findByEmail(userEmail);
        if (optionalUser.isEmpty()) {
            throw new IllegalStateException("존재하지 않는 유저입니다 : " + userEmail);
        }
        User user = optionalUser.get();
        authService.modifyAuthProvider(user.getId(), AuthProvider.donkey);
        redisUtil.deleteData(key);
    }
}
