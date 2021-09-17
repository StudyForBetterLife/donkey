package com.donkey.domain.community;

import com.donkey.domain.user.User;
import com.donkey.domain.BaseEntity;
import com.donkey.domain.user.UserMajor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Major extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "major_id")
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id")
    private University university;

    @OneToMany(mappedBy = "major")
    private List<User> userList = new ArrayList<>();

    @OneToMany(mappedBy = "major")
    private List<UserMajor> userMajors = new ArrayList<>();
}
