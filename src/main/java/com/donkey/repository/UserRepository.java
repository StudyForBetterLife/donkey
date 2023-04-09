package com.donkey.repository;

import com.donkey.domain.user.AuthProvider;
import com.donkey.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findUserByUsrIdAndTelNum(String usrId, String telNum);

    Optional<User> findUserByEmailAndUsrIdAndTelNum(String email, String usrId, String telNum);

    Boolean existsByEmail(String email);

    @Modifying
    @Query("update User u set u.password = :pass where u.id = :id")
    void updatePassword(@Param("id") Long id, @Param("pass") String password);

    @Modifying
    @Query("update User u set u.authProvider = :auth where u.id = :id")
    void updateAuthProvider(@Param("id") Long id, @Param("auth") AuthProvider authProvider);
}
