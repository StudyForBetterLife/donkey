package com.donkey.domain.user;

import com.donkey.domain.BaseEntity;
import com.donkey.domain.community.Major;
import com.donkey.domain.community.University;
import com.donkey.domain.enums.AuthProvider;
import com.donkey.domain.enums.UserType;
import com.donkey.domain.post.Post;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    private String uId; // 사용자 id
    private String password;
    private String profilePicture;
    private String address;
    private String introduction;
    private String fcmToken;
    private int score;

    @Enumerated(EnumType.STRING)
    private UserType userType = UserType.UNCERTIFIED;

    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id")
    private University university;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id")
    private Major major;

    @OneToMany(mappedBy = "user")
    private List<UserMajor> userMajors = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Post> posts = new ArrayList<>();

    @Builder
    public User(String email, String name, String uId, String password, String profilePicture, String address, String introduction, String fcmToken, int score, UserType userType, AuthProvider authProvider) {
        this.email = email;
        this.name = name;
        this.uId = uId;
        this.password = password;
        this.profilePicture = profilePicture;
        this.address = address;
        this.introduction = introduction;
        this.fcmToken = fcmToken;
        this.score = score;
        this.userType = userType;
        this.authProvider = authProvider;
    }


    public User update(User entity) {
        if (entity.getName() != null)
            this.name = entity.getName();
        if (entity.getUId() != null)
            this.uId = entity.getUId();
        if (entity.getPassword() != null)
            this.password = entity.getPassword();
        if (entity.getProfilePicture() != null)
            this.profilePicture = entity.getProfilePicture();
        if (entity.getAddress() != null)
            this.address = entity.getAddress();
        if (entity.getIntroduction() != null)
            this.introduction = entity.getIntroduction();
        if (entity.getFcmToken() != null)
            this.fcmToken = entity.getFcmToken();

        return this;
    }

    public void updateScore(int score) {
        this.score = score;
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getUserTypeKey() {
        return this.userType.getKey();
    }

    public void universityCertified() {
        this.userType = UserType.CERTIFIED;
    }


}
