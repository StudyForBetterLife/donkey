package com.donkey.domain.community;

import com.donkey.domain.user.User;
import com.donkey.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class University extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "university_id")
    private Long id;

    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", unique = true)
    private Community community;

    @OneToMany(mappedBy = "university")
    private List<User> userList = new ArrayList<>();

    @OneToMany(mappedBy = "university")
    private List<Major> majors = new ArrayList<>();

}
