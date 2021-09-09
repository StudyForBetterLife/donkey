package com.donkey.domain.user;

import com.donkey.domain.BaseEntity;
import com.donkey.domain.community.Major;
import com.donkey.domain.community.University;
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

    private String uId; // 사용자 아이디
    private String password;
    private String profilePicture;
    private String address;
    private String introduction;
    private String fcmToken;
    private int score;

    @Enumerated(EnumType.STRING)
    private UserType userType;

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
    public User(String email, String password, String profilePicture, String uId, String location, String introduction) {
        this.email = email;
        this.password = password;
        this.profilePicture = profilePicture;
        this.uId = uId;
        this.address = location;
        this.introduction = introduction;
    }

    public String getUserType() {
        return this.userType.getValue();
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }


}
