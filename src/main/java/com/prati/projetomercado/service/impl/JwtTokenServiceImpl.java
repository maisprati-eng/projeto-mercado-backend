package com.prati.projetomercado.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.prati.projetomercado.config.UserDetailsImpl;
import com.prati.projetomercado.entity.AuthUser;
import com.prati.projetomercado.entity.RefreshToken;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class JwtTokenServiceImpl {
    //TODO colocar secret key em um arquivo a parte
    public static final String SECRET_KEY = "MUDAR_DEPOIS";

    public static final String ISSUER = "prati-projeto-mercado";

    public String generateToken(AuthUser authUser, Instant expirationDate) {
        try {
            var algorithm = Algorithm.HMAC256(SECRET_KEY);
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withIssuedAt(creationDate())
                    .withExpiresAt(expirationDate)
                    .withSubject(authUser.getEmail())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new JWTCreationException("Token creation error", exception);
        }
    }

    public String getSubjectFromToken(String token) {
        try {
            var algorithm = Algorithm.HMAC256(SECRET_KEY);
            return JWT.require(algorithm).withIssuer(ISSUER).build().verify(token).getSubject();
        } catch (JWTVerificationException e) {
            throw new JWTVerificationException("Invalid/expired token");
            
        }
    }
    
    public Instant creationDate() {
        return ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).toInstant();
    }

    public Instant expirationAcessTokenDate() {
        return ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).plusHours(1).toInstant();
    }

    public RefreshToken generateNewRefreshToken(AuthUser user){
        var refreshToken = new RefreshToken();
        refreshToken.setAuthUser(user);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(3600 * 12));
        return refreshToken;
    }


}
