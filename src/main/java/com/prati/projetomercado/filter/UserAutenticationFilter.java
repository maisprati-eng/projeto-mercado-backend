package com.prati.projetomercado.filter;

import com.prati.projetomercado.config.SecurityConfiguration;
import com.prati.projetomercado.config.UserDetailsImpl;
import com.prati.projetomercado.repository.AccessTokenRepository;
import com.prati.projetomercado.repository.AuthUserRepository;
import com.prati.projetomercado.service.impl.JwtTokenServiceImpl;
import com.prati.projetomercado.utils.TokenUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;

@Component
public class UserAutenticationFilter extends OncePerRequestFilter {

    private JwtTokenServiceImpl jwtTokenService;

    private AuthUserRepository authUserRepository;
    private AccessTokenRepository accessTokenRepository;

    public UserAutenticationFilter(JwtTokenServiceImpl jwtTokenService, AuthUserRepository authUserRepository, AccessTokenRepository accessTokenRepository) {
        this.jwtTokenService = jwtTokenService;
        this.authUserRepository = authUserRepository;
        this.accessTokenRepository = accessTokenRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        var permittedMatches = Arrays.stream(SecurityConfiguration.PUBLIC_ENDPOINTS);

        var permitted = permittedMatches.anyMatch(stringURI-> PathPatternRequestMatcher.withDefaults().matcher(stringURI).matches(request));
        return permitted;

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = recoveryToken(request);

        if (token == null) {
            throw new RuntimeException("no token");
        }

        var subject = jwtTokenService.getSubjectFromToken(token);
        var user = authUserRepository.findByEmail(subject).get();
        var accessTokenFromRepo = accessTokenRepository.findByAuthUser(user);

        if (accessTokenFromRepo.getExpiredDate().isBefore(Instant.now()))
            throw new RuntimeException("accessToken expired");

        var userDetails = new UserDetailsImpl(user);

        var auth = new UsernamePasswordAuthenticationToken(userDetails.getUsername(), null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    private String recoveryToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        return TokenUtils.recoveryToken(authorizationHeader);
    }

}
