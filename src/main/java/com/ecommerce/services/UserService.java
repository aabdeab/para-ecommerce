package com.ecommerce.services;

import com.ecommerce.models.User;
import com.ecommerce.models.UserRole;
import com.ecommerce.repositories.UserRepository;
import com.ecommerce.exceptions.UserNotFoundException;
import com.ecommerce.exceptions.UserNotValidException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User addUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserNotValidException("User with email " + user.getEmail() + " already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        log.info("Adding user with email: {}", user.getEmail());
        return userRepository.save(user);

    }

    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public void deleteUser(Long userId) {
        User existingUser = checkUserExistence(userId);
        log.info("Deleting user with id: {}", userId);
        userRepository.delete(existingUser);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public User updateUser(Long userId, User newUser) {
        User existingUser = checkUserExistence(userId);
        newUser.setUserId(existingUser.getUserId());
        if (!newUser.getPassword().equals(existingUser.getPassword())) {
            newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        }

        log.info("Updating user with id: {}", userId);
        return userRepository.save(newUser);
    }
    @Transactional
    public User updateUserRole(Long userId, UserRole newRole){
        User existingUser = checkUserExistence(userId);
        existingUser.setRole(newRole);
        log.info("user with id: {} has new role: {}", userId,newRole);
        return userRepository.save(existingUser);
    }
    @Cacheable(value="users",key = "#userId")
    public Optional<User> getUserByEmail(String email){
        return userRepository.findByEmail(email);
    }

    @Cacheable(value = "users", key = "#userId")
    public User getUserById(Long userId) {
        return checkUserExistence(userId);
    }

    private User checkUserExistence(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id=" + userId + " not found"));
    }
    private User checkUserExistence(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email=" + email + " not found"));
    }


}