package com.donkey.service;

import com.donkey.domain.user.AuthProvider;
import com.donkey.domain.user.User;
import com.donkey.repository.UserRepository;
import com.donkey.util.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final RedisUtil redisUtil;

    @Transactional
    public Long save(User user) {
        return userRepository.save(user).getId();
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public Page<User> findAllPage(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public List<User> findAll() {
        return userRepository.findAll();
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

    @Transactional
    public void modifyAuthProvider(Long id, AuthProvider authProvider) {
        userRepository.updateAuthProvider(id, authProvider);
    }
}
