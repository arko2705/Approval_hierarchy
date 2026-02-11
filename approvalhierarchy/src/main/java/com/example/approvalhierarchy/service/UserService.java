package com.example.approvalhierarchy.service;

import com.example.approvalhierarchy.model.Role;
import com.example.approvalhierarchy.model.User;
import com.example.approvalhierarchy.repository.RoleRepository;
import com.example.approvalhierarchy.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User registerNewUser(User user, String roleName) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        Role userRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        
        // Create a new HashSet with the role
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);
        
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}