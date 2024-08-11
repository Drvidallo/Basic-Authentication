package com.authentication.service.impl;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.authentication.errorhandling.TokenRefreshException;
import com.authentication.model.RefreshToken;
import com.authentication.model.User;
import com.authentication.repository.RefreshTokenRepository;
import com.authentication.repository.UserRepository;
import com.authentication.service.RefreshTokenService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.token.refresh.duration.ms}")
    private Long TOKEN_EXPIRATION_MS;

    @Override
    public RefreshToken save(RefreshToken token) {
        return refreshTokenRepository.save(token);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        log.debug("findByToken: " + token);
        if (token == null) {
            log.error("Invalid token input");
            return null;
        }

        return refreshTokenRepository.findByToken(token);
    }

    @Override
    public RefreshToken findByUserId(Long id) {
        log.debug("findByUserId: " + id);
        if (id == null) {
            log.error("Invalid id input");
            return null;
        }

        return refreshTokenRepository.findByUserId(id).orElseThrow(EntityNotFoundException::new);
    }

    @Override
    public RefreshToken generateToken(Long userId) {
        User user = userRepository.findById(userId).get();
        return generateToken(user);
    }

    @Override
    public RefreshToken generateToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        // user.setRefreshToken(refreshToken);

//        refreshToken.setUser(user);
        refreshToken.setExpirationDate(Instant.now().plusMillis(TOKEN_EXPIRATION_MS));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshToken;
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpirationDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(),
                    "Refresh token has expired. Please make a new signin request");
        }

        return token;
    }

    @Override
    public RefreshToken expireToken(RefreshToken token) {
        User user = userRepository.findByRefreshToken(token);

        token.setExpirationDate(Instant.now().minus(1, ChronoUnit.DAYS));
        user.setRefreshToken(token);
        
        userRepository.save(user);

        return token;
    }

    @Transactional
    @Override
    public RefreshToken delete(RefreshToken token) {
        refreshTokenRepository.delete(token);
        return token;
    }

    @Override
    public RefreshToken findByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(EntityNotFoundException::new);
        return refreshTokenRepository.findByToken(user.getRefreshToken().getToken()).orElseThrow(EntityNotFoundException::new);
    }
}
