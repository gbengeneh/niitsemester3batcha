package com.semester3.user_service.service;

import com.semester3.user_service.entity.RefreshToken;
import com.semester3.user_service.entity.User;
import com.semester3.user_service.repository.RefreshTokenRepository;
import com.semester3.user_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${jwt.refreshExpirationMs:604800000}") // default 7 days
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }
    //create a new refresh token for a user
    @Transactional
    public RefreshToken createRefreshToken(User user){
        // Delete any existing token for this user (rotation)
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        return refreshTokenRepository.save(refreshToken);
    }
    public RefreshToken verifyAndRotate(String oldToken){
        RefreshToken token = refreshTokenRepository.findByToken(oldToken)
                .orElseThrow(() ->new RuntimeException("Invalid refresh token"));
        if(token.getExpiryDate().isBefore(Instant.now())){
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired");
        }
        // Rotate token (delete old, create new)
        refreshTokenRepository.delete(token);
        return createRefreshToken(token.getUser());
    }
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    //  Delete all refresh tokens for a given user
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }


}