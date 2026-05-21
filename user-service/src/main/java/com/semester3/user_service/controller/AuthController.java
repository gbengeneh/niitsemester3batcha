package com.semester3.user_service.controller;

import com.semester3.user_service.dto.*;
import com.semester3.user_service.entity.RefreshToken;
import com.semester3.user_service.entity.User;
import com.semester3.user_service.service.RefreshTokenService;
import com.semester3.user_service.service.UserService;
import com.semester3.user_service.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, UserService userService, RefreshTokenService refreshTokenService, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
        this.jwtUtil = jwtUtil;
    }

    //Register
    public ResponseEntity<AuthUserResponse> register(@RequestBody RegisterRequest request){
        User created = userService.registerUser(request);

       //generate token
       String accessToken = jwtUtil.generateAccessToken(created.getEmail());
       RefreshToken refreshToken = refreshTokenService.createRefreshToken(created);

       AuthUserResponse response = new AuthUserResponse(
               created.getId(),
               created.getUsername(),
               created.getEmail(),
               created.getRoles(),
               accessToken,
               refreshToken.getToken()
       );
       return ResponseEntity.ok(response);
    }
    //Login
    public ResponseEntity<AuthUserResponse> login(@RequestBody LoginRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
     //   load user
     User user = userService.findByUsername(request.getUsername())
             .orElseThrow(()-> new RuntimeException("User not found"));
     //Generate tokens
        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        // prepare response
        AuthUserResponse response = new AuthUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles(),
                accessToken,
                refreshToken.getToken()
        );
        return ResponseEntity.ok(response);
    }
   // Refresh token (rotating)
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody TokenRefreshRequest request){
        String requestToken = request.getRefreshToken();

        //validate and rotate
        RefreshToken newToken  = refreshTokenService.verifyAndRotate(requestToken);

        //generate new access token
        User user = newToken.getUser();
        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail());

        //refresh new jwt + new refresh token
        return ResponseEntity.ok(new AuthResponse(newAccessToken, newToken.getToken()));
    }

    // logout
    public ResponseEntity<String> logout(@RequestBody TokenRefreshRequest request){
        if(request.getRefreshToken() !=null && !request.getRefreshToken().isBlank()){
            refreshTokenService.findByToken(request.getRefreshToken())
                    .ifPresent(token -> refreshTokenService.deleteByUser(token.getUser()));
        }
        return  ResponseEntity.ok("Logged out successfully");
    }

}
