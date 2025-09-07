package com.prati.projetomercado.config;

import com.prati.projetomercado.entity.AuthUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class UserDetailsImpl implements UserDetails {

    private AuthUser authUser;

    public UserDetailsImpl(AuthUser authUser) {
        this.authUser = authUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authUser.getRoles().stream().map(role ->
                new SimpleGrantedAuthority(role.getRoleName().name())).collect(Collectors.toList()
        );
    }

    @Override
    public String getUsername() {
        return authUser.getEmail();
    }

    @Override
    public String getPassword() {
        return authUser.getPassword();
    }

    public AuthUser getAuthUser() { return authUser;}

}
