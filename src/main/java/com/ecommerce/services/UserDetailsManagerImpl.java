package com.ecommerce.services;

import com.ecommerce.models.SecurityUser;
import com.ecommerce.models.User;
import com.ecommerce.repositories.UserRepository;
import io.jsonwebtoken.lang.Assert;
import lombok.AllArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class UserDetailsManagerImpl implements UserDetailsManager {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email " + email));

        return SecurityUser.builder()
                .user(user)
                .build();
    }

    @Override
    public void createUser(UserDetails userDetails) {
        Assert.notNull(userDetails, "user cannot be null");
        User user = ((SecurityUser) userDetails).user();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Override
    public void updateUser(UserDetails userDetails) {
        Assert.notNull(userDetails, "user cannot be null");

        User userToUpdate = ((SecurityUser) userDetails).user();
        User existing = userRepository.findByEmail(userToUpdate.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        existing.setFirstName(userToUpdate.getFirstName());
        existing.setLastName(userToUpdate.getLastName());
        existing.setRole(userToUpdate.getRole());
        if (userToUpdate.getPassword() != null) {
            existing.setPassword(passwordEncoder.encode(userToUpdate.getPassword()));
        }

        userRepository.save(existing);
    }

    @Override
    public void deleteUser(String username) {
        userRepository.findByEmail(username)
                .ifPresent(userRepository::delete);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        String currentUserEmail =
                SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }


    @Override
    public boolean userExists(String username) {
        return userRepository.existsByEmail(username);
    }

}
