package com.donkey.domain.user;

import com.donkey.domain.BaseEntity;
import com.donkey.domain.community.Major;
import com.donkey.domain.community.University;
import com.donkey.domain.post.Post;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
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

    private String nickName;
    private String usrId; // 사용자 id
    private String password;

    @Column(nullable = false)
    private String telNum;
    private String imageUrl;
    private String introduction;
    private String fcmToken;
    private int score;

    @Embedded
    private Address address;

    @Embedded
    private UniversityInfo universityInfo;

    @Enumerated(EnumType.STRING)
    private UserType userType = UserType.UNCERTIFIED;

    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider = AuthProvider.none;

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
    public User(String email, String name, String nickName, String usrId, String password, String telNum, String imageUrl, String introduction, String fcmToken, int score, Address address, UniversityInfo universityInfo, AuthProvider authProvider) {
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
        this.fcmToken = fcmToken;
        this.universityInfo = universityInfo;
        this.authProvider = authProvider;
    }


    public User update(User entity) {
        if (entity.getName() != null)
            this.name = entity.getName();
        if (entity.getUsrId() != null)
            this.usrId = entity.getUsrId();
        if (entity.getPassword() != null)
            this.password = entity.getPassword();
        if (entity.getImageUrl() != null)
            this.imageUrl = entity.getImageUrl();
        if (entity.getAddress() != null)
            this.address = entity.getAddress();
        if (entity.getIntroduction() != null)
            this.introduction = entity.getIntroduction();
        if (entity.getFcmToken() != null)
            this.fcmToken = entity.getFcmToken();

        return this;
    }

    public void updatePassword(String password) {
        this.password = password;
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

    public void updateAuthProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }


}
