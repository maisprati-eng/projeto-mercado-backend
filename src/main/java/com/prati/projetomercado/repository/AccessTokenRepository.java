package com.prati.projetomercado.repository;

import com.prati.projetomercado.entity.AccessToken;
import com.prati.projetomercado.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccessTokenRepository extends JpaRepository<AccessToken, UUID> {
    AccessToken findByAuthUser(AuthUser authUser);

    Optional<AccessToken> findByToken(String token);
}
