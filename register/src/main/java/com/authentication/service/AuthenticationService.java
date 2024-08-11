package com.authentication.service;

import java.util.List;
import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.authentication.errorhandling.TokenRefreshException;
import com.authentication.errorhandling.UserExistException;
// import com.authentication.model.Interview;
// import com.authentication.model.Interviewer;
import com.authentication.model.RefreshToken;
import com.authentication.model.Role;
import com.authentication.model.User;
import com.authentication.model.payload.request.RegisterRequest;
import com.authentication.model.payload.request.LoginRequest;
import com.authentication.model.payload.response.JwtResponse;
import com.authentication.repository.RoleRepository;
import com.authentication.repository.UserRepository;
import com.authentication.security.UserDetailsImpl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()
                || userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserExistException("User already exists.");
        }
        Role role = roleRepository.findByName(request.getRole()).orElse(null);
        User user = new User()
                .setEmail(request.getEmail())
                .setUsername(request.getUsername())
                .setPassword(passwordEncoder.encode(request.getPassword()))
                .setRoles(Set.of(role));
        User newUser = userRepository.save(user);
        return newUser;
    }

    @Transactional
    public JwtResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        List<String> roles = user.getRoles().stream().map(role -> role.getName().name()).toList();
        RefreshToken refreshToken = null;
        try {
            refreshToken = refreshTokenService.findByUserId(user.getId());
            refreshToken = refreshTokenService.verifyExpiration(refreshToken);
        } catch (EntityNotFoundException | TokenRefreshException e) {
            RefreshToken newToken = refreshTokenService.generateToken(user);
            newToken.setUser(user);

            user.setRefreshToken(null);
            userRepository.save(user);
            userRepository.flush();

            user.setRefreshToken(newToken);
            userRepository.save(user);
            userRepository.flush();
            refreshToken = newToken;
        }

        var jwtToken = jwtService.generateToken(UserDetailsImpl.build(user));

        return JwtResponse.builder()
                .id(user.getId())
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles).build();
    }
}