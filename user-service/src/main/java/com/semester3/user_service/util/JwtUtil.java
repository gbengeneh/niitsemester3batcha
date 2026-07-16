package com.semester3.user_service.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {
    private static final String ROLES_CLAIM = "roles";

    private final Key key;
    private final long accessExpirationMs;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                  @Value("${app.jwt.accessExpirationMs}") long accessExpirationMs){
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessExpirationMs = accessExpirationMs;
    }

    // Roles are embedded as a claim so every downstream microservice can authorize
    // requests straight from the token, without calling back into user-service.
    public String generateAccessToken(String username, Collection<String> roles){
        Date now = new Date();
        Date expiry= new Date(now.getTime()+accessExpirationMs);
        return Jwts.builder()
                .setSubject(username)
                .claim(ROLES_CLAIM, roles)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key)
                .compact();
    }
    public String extractUsername(String token){
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token){
        Object roles = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().get(ROLES_CLAIM);
        return roles == null ? List.of() : (List<String>) roles;
    }

    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        }catch(JwtException e){
            return false;
        }
    }
}