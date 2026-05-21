package com.semester3.user_service.repository;

import com.semester3.user_service.entity.RefreshToken;
import com.semester3.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface RefreshTokenRepository extends JpaRepository<RefreshToken , Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User  user);
}
