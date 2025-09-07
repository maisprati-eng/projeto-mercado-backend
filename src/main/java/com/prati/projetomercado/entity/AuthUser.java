package com.prati.projetomercado.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Table(name = "authUsers")
@Entity(name = "authUser")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class AuthUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String email;
    
    private String password;

    @CreationTimestamp
    private Instant creationDate;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<AccountRole> roles;

    @OneToMany(mappedBy = "authUser", cascade = CascadeType.ALL)
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @OneToMany(mappedBy = "authUser", cascade = CascadeType.ALL)
    private List<AccessToken> accessTokens = new ArrayList<>();

    @OneToMany(mappedBy = "createdByUser")
    private List<Supermarket> createdSupermarkets;

    @OneToMany(mappedBy = "user")
    private List<Purchase> purchases;
}
