package com.prati.projetomercado.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
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
import com.prati.projetomercado.repository.AccessTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;


@Service
@RequiredArgsConstructor
public class JwtTokenServiceImpl {
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.issuer}")
    private String ISSUER;

    @Value("${jwt.expiration.access-token.hours}")
    private int ACCESS_TOKEN_EXPIRATION_HOURS;

    @Value("${jwt.expiration.refresh-token.hours}")
    private int REFRESH_TOKEN_EXPIRATION_HOURS;

    private final AccessTokenRepository accessTokenRepository;

    private final Set<String> blacklist = new HashSet<>();

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
            throw new JWTVerificationException("Invalid/expired token");
            
        }
    }
    
    public Instant creationDate() {
        return ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).toInstant();
    }

    public Instant expirationAccessTokenDate() {
        return ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).plusHours(ACCESS_TOKEN_EXPIRATION_HOURS).toInstant();
    }

    public RefreshToken generateNewRefreshToken(AuthUser user){
        var refreshToken = new RefreshToken();
        refreshToken.setAuthUser(user);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(3600L * REFRESH_TOKEN_EXPIRATION_HOURS));
        return refreshToken;
    }

    public void invalidateToken(String token) {
        blacklist.add(token);
        accessTokenRepository.findByToken(token)
                .ifPresent(accessTokenRepository::delete);
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
