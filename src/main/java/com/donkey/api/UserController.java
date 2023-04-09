package com.donkey.api;

import com.donkey.api.dto.Result;
import com.donkey.api.dto.UserProfileReq;
import com.donkey.api.dto.UserProfileRes;
import com.donkey.domain.user.User;
import com.donkey.service.AuthService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;

    @GetMapping
    @ApiOperation(value = "유저 프로필 리스트 (페이징)", notes = "요청 파라미터 예시 : /api/users?page=0&size=3&sort=email,desc&sort=name,desc")
    public Page<UserProfileRes> getAllUserProfile(Pageable pageable) {
        Page<User> all = authService.findAllPage(pageable);
        return all.map(UserProfileRes::new);
    }

    @PostMapping("/profile")
    @ApiOperation(value = "유저 프로필 (한 명)")
    public Result<UserProfileRes> getProfile(@RequestBody UserProfileReq req) {
        Optional<User> optionalUser = authService.findByEmail(req.getEmail());
        if (optionalUser.isEmpty()) {
            return new Result<>(false, "존재하지 않는 유저입니다.", null);
        }
        User user = optionalUser.get();
        return new Result<>(true, "유저 조회 성공", new UserProfileRes(user));
    }
}
