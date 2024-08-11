package com.authentication.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.authentication.model.RefreshToken;
import com.authentication.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
	Optional<User> findByEmail(String email);

    User findByRefreshToken(RefreshToken refreshToken);

	Boolean existsByUsername(String username);

	Boolean existsByEmail(String email);
}
