package com.prati.projetomercado.controller;

import com.prati.projetomercado.dto.request.CreateUserRequest;
import com.prati.projetomercado.dto.request.LoginUserRequest;
import com.prati.projetomercado.dto.request.RefreshTokenRequest;
import com.prati.projetomercado.model.JwtToken;
import com.prati.projetomercado.repository.AuthUserRepository;
import com.prati.projetomercado.service.UserService;
import com.prati.projetomercado.service.impl.JwtTokenServiceImpl;
import com.prati.projetomercado.utils.TokenUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtTokenServiceImpl jwtTokenServiceImpl;
    private AuthUserRepository userRepository;
    private UserService userService;
    private JwtTokenServiceImpl tokenService;

    public AuthController(AuthUserRepository userRepository, UserService userService, JwtTokenServiceImpl tokenService, JwtTokenServiceImpl jwtTokenServiceImpl) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.tokenService = tokenService;
        this.jwtTokenServiceImpl = jwtTokenServiceImpl;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody CreateUserRequest userRequest) {
        userService.registerUser(userRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);

    }

    @PostMapping("/login")
    public ResponseEntity<JwtToken> login(@RequestBody LoginUserRequest userRequest) throws Exception {
       var jwtToken = userService.login(userRequest);
       return new ResponseEntity<>(jwtToken, HttpStatus.OK);
       
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<JwtToken> refresh(@RequestHeader String Authorization, @RequestBody RefreshTokenRequest refreshToken) throws Exception {
        var newJwtToken = userService.useRefreshToken(TokenUtils.recoveryToken(Authorization), UUID.fromString(refreshToken.refreshToken()));
        return new ResponseEntity<>(newJwtToken, HttpStatus.OK);
    }

    @PostMapping("/test-autenticated")
    public ResponseEntity<String> test(@RequestHeader String Authorization, @RequestBody String alow) throws Exception {
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }


}
