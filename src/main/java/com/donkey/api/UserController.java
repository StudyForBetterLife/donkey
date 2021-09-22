package com.donkey.api;

import com.donkey.api.dto.*;
import com.donkey.domain.user.User;
import com.donkey.service.UserService;
import com.donkey.util.RandomNumberGenerator;
import com.donkey.util.encrypt.EncryptHelper;
import com.donkey.util.mail.MailDto;
import com.donkey.util.mail.MailService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final EncryptHelper encryptHelper;
    private final MailService mailService;

    @GetMapping
    @ApiOperation(value = "유저 프로필 리스트 (페이징)", notes = "요청 파라미터 예시 : /api/users?page=0&size=3&sort=id,desc&sort=username,desc")
    public Page<UserProfileRes> getAllUserProfile(Pageable pageable) {
        Page<User> all = userService.findAllPage(pageable);
        return all.map(UserProfileRes::new);
    }

    @PostMapping("/profile")
    @ApiOperation(value = "유저 프로필 (한 명)")
    public Result<UserProfileRes> getProfile(@RequestBody UserProfileReq req) {
        Optional<User> optionalUser = userService.findByEmail(req.getEmail());
        if (optionalUser.isEmpty()) {
            return new Result<>(false, "존재하지 않는 유저입니다.", null);
        }
        User user = optionalUser.get();
        return new Result<>(true,"유저 조회 성공", new UserProfileRes(user));
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

        if (userService.existsByEmail(req.getEmail())) {
            return CreateUserRes.builder()
                    .success(false)
                    .message("이미 존재하는 유저입니다")
                    .build();
        }

        User reqUser = User.builder()
                .email(req.getEmail())
                .name(req.getName())
                .usrId(req.getUserId())
                .nickName(req.getNickName())
                .password(encryptHelper.encrypt(req.getPassword()))
                .telNum(req.getTelNum())
                .build();
        Long id = userService.save(reqUser);

        return CreateUserRes.builder()
                .success(true)
                .message("회원 가입에 성공했습니다.")
                .id(id)
                .build();
    }

    @GetMapping("/register/{email}")
    @ApiOperation(value = "이메일 중복확인")
    public BaseRes checkEmail(@PathVariable("email") String email) {
        boolean exists = userService.existsByEmail(email);
        String message = exists ? "이미 존재하는 이메일입니다." : "사용 가능한 이메일입니다.";
        return BaseRes.builder()
                .success(!exists)
                .message(message)
                .build();
    }

    @PostMapping("/login")
    public BaseRes login(@RequestBody LoginUserReq req) {
        Optional<User> optionalUser = userService.findByEmail(req.getEmail());
        if (optionalUser.isEmpty()) {
            return BaseRes.builder()
                    .success(false)
                    .message("존재하지 않는 이메일입니다.")
                    .build();
        }

        User user = optionalUser.get();
        boolean isCorrect = encryptHelper.isMatch(req.getPassword(), user.getPassword());
        if (!isCorrect) {
            return BaseRes.builder()
                    .success(false)
                    .message("잘못된 비밀번호 입니다.")
                    .build();
        }

        return BaseRes.builder()
                .success(true)
                .message("'" + user.getName() + "' 님 환영합니다.")
                .build();

    }

    @PostMapping("/find-email")
    public BaseRes findUserEmail(@RequestBody FindUserEmailReq req) {
        Optional<User> optionalUser = userService.findUserByUserIdAndTelNum(req.getUserId(), req.getTelNum());
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
        Optional<User> optionalUser = userService.findUserByEmailAndUserIdAndTelNum(req.getEmail(), req.getUserId(), req.getTelNum());
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
        userService.updateTemporaryPassword(user.getId(), encrypted);

        // 유저 메일로 변경된 패스워드 보내기
        sendMail(user.getEmail(), randomStr);

        return BaseRes.builder()
                .success(true)
                .message(user.getEmail() + " 로 임시 패스워드 전송")
                .build();
    }

    private void sendMail(String address, String message) {
        new Thread(() -> {
            try {
                mailService.sendEmailForPassword(MailDto.builder()
                        .address(address)
                        .token(message)
                        .build());
                log.error("메일 보내기 성공 : 변경된 패스워드");
            } catch (MessagingException e) {
                log.error("메일 보내기 실패 : 변경된 패스워드");
                e.printStackTrace();
            }
        }).start();
    }
}
