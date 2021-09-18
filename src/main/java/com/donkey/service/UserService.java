package com.donkey.service;

import com.donkey.domain.user.User;
import com.donkey.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public Long save(User user) {
        return userRepository.save(user).getId();
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findUserByUserIdAndTelNum(String userId, String telNum) {
        return userRepository.findUserByUsrIdAndTelNum(userId, telNum);
    }

    public Optional<User> findUserByEmailAndUserIdAndTelNum(String email, String usrId, String telNum) {
        return userRepository.findUserByEmailAndUsrIdAndTelNum(email, usrId, telNum);
    }

    @Transactional
    public void updateTemporaryPassword(Long id, String password) {
        userRepository.updatePassword(id, password);
    }
}
