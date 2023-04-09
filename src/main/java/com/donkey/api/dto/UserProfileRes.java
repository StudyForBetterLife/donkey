package com.donkey.api.dto;

import com.donkey.domain.user.User;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Data
@NoArgsConstructor
public class UserProfileRes {
    /*
            this.email = email;
        this.name = name;
        this.nickName = nickName;
        this.usrId = usrId;
        this.password = password;
        this.telNum = telNum;
        this.imageUrl = imageUrl;
        this.introduction = introduction;
        this.score = score;
        this.address = address;
     */

    private String email;
    private String name;
    private String nickName;
    private String userId;
    private String telNum;
    @ApiModelProperty(example = "유저 프로필 이미지 경로")
    private String profileImagePath;
    private String introduction;
    @ApiModelProperty(example = "유저 평가 점수")
    private int score;
    private String address;

    @Builder
    public UserProfileRes(User entity) {
        this.email = entity.getEmail();
        this.name = entity.getName();
        this.nickName = entity.getNickName();
        this.userId = entity.getUsrId();
        this.telNum = entity.getTelNum();
        this.profileImagePath = entity.getImageUrl();
        this.introduction = entity.getIntroduction();
        this.score = entity.getScore();
        if (!Objects.isNull(entity.getAddress()))
            this.address = entity.getAddress().toWholeAddress();
    }
}
