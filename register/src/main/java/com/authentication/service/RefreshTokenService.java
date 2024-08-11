package com.authentication.service;

import java.util.Optional;

import com.authentication.model.RefreshToken;
import com.authentication.model.User;

public interface RefreshTokenService {

    RefreshToken delete(RefreshToken token);
    RefreshToken save(RefreshToken token);
    Optional<RefreshToken> findByToken(String token);
    RefreshToken expireToken(RefreshToken token);
    RefreshToken findByUserId(Long id);
    RefreshToken findByUsername(String username);
    RefreshToken generateToken(Long id);
    RefreshToken generateToken(User user);
    RefreshToken verifyExpiration(RefreshToken token);
} 