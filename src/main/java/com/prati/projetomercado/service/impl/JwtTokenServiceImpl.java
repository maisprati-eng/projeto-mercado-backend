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
import java.util.Set;
import java.util.HashSet;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.exceptions.JWTDecodeException;


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
        //deverá checar se o token está na blacklist antes de validar
        if (isTokenInvalid(token)) {
            throw new JWTVerificationException("Token foi invalidado (logout realizado)");
        }
        try {
            var algorithm = Algorithm.HMAC256(SECRET_KEY);
            return JWT.require(algorithm).withIssuer(ISSUER).build().verify(token).getSubject();
        } catch (JWTVerificationException e) {
            e.printStackTrace();
            throw new JWTVerificationException("Invalid/expired token");
            
        }
    }
    
    public Instant creationDate() {
        return ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).toInstant();
    }

    public Instant expirationAccessTokenDate() {
        return ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).plusSeconds(5).toInstant();
    }

    public RefreshToken generateNewRefreshToken(AuthUser user){
        var refreshToken = new RefreshToken();
        refreshToken.setAuthUser(user);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(3600 * 12));
        return refreshToken;
    }

    private final Set<String> blacklist = new HashSet<>();

    public void invalidateToken(String token) {
        blacklist.add(token);
    }

    public boolean isTokenInvalid(String token) {
        return blacklist.contains(token);
    }

    public String getSubjectFromExpiredToken(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getSubject();
        } catch (JWTDecodeException exception){
            throw new JWTVerificationException("Token inválido ou malformado.");
        }
    }

}
