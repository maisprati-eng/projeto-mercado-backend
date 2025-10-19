package com.prati.projetomercado.repository;

import com.prati.projetomercado.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {
    Optional<AuthUser> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<AuthUser> findByConfirmationToken(String token);

}
