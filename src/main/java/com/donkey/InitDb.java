package com.donkey;

import com.donkey.domain.user.AuthProvider;
import com.donkey.domain.user.User;
import com.donkey.util.encrypt.EncryptHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

@Component
@RequiredArgsConstructor
public class InitDb {

    private final InitService initService;
    @PostConstruct
    public void init() {
        initService.dbInit1();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final EntityManager em;
        private final EncryptHelper encryptHelper;

        public void dbInit1() {
            User user1 = User.builder()
                    .email("guswns11@naver.com")
                    .name("하현준")
                    .nickName("hj")
                    .usrId("guswns11")
                    .password(encryptHelper.encrypt("1"))
                    .authProvider(AuthProvider.donkey)
                    .telNum("010123123")
                    .build();

            em.persist(user1);

            User user2 = User.builder()
                    .email("www@naver.com")
                    .name("당나귀")
                    .nickName("dk")
                    .usrId("dkny1")
                    .password(encryptHelper.encrypt("1"))
                    .authProvider(AuthProvider.donkey)
                    .telNum("010321321")
                    .build();

            em.persist(user2);
        }
    }
}
