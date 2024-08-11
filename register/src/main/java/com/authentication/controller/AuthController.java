package com.authentication.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.authentication.errorhandling.TokenRefreshException;
import com.authentication.model.RefreshToken;
import com.authentication.model.User;
import com.authentication.model.payload.request.RegisterRequest;
import com.authentication.model.payload.request.LoginRequest;
import com.authentication.model.payload.request.LogoutRequest;
import com.authentication.model.payload.request.RefreshTokenRequest;
import com.authentication.model.payload.response.JwtResponse;
import com.authentication.model.payload.response.RefreshTokenResponse;
import com.authentication.model.payload.response.UserRegisterResponse;
import com.authentication.security.UserDetailsImpl;
import com.authentication.service.AuthenticationService;
import com.authentication.service.JwtService;
import com.authentication.service.RefreshTokenService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> authenticateUser(
            @Valid @RequestBody LoginRequest request) {
        LOGGER.info(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponse> register(
            @RequestBody RegisterRequest request) {
        User user = authenticationService.register(request);

        UserRegisterResponse response = new UserRegisterResponse(user.getId(), user.getUsername());
        response.setMessage("User created successfully.");
        response.setStatus("success");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        // if(refreshToken == null) {
        // throw new TokenRefreshException(refreshToken,
        // "Refresh token is not in database!");
        // }

        return refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtService.generateToken(UserDetailsImpl.build(user));
                    return ResponseEntity.ok(new RefreshTokenResponse(token, refreshToken));
                }).orElseThrow(() -> new TokenRefreshException(refreshToken,
                        "Refresh token is not in database!"));
    }

    @CrossOrigin(origins = "http://localhost:3000/", maxAge = 3600)
    @PostMapping("/signout")
    public ResponseEntity<String> logoutUser(@RequestBody LogoutRequest request) {
        RefreshToken token = refreshTokenService.findByToken(request.getRefreshToken()).orElseThrow(
                () -> new TokenRefreshException(request.getRefreshToken(),
                        "Refresh token is not in database!"));
        refreshTokenService.expireToken(token);
        return ResponseEntity.ok("Logout successful!");
    }
}
