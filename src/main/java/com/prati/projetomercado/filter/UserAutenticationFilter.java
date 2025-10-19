package com.prati.projetomercado.filter;

import com.prati.projetomercado.config.SecurityConfiguration;
import com.prati.projetomercado.repository.AccessTokenRepository;
import com.prati.projetomercado.service.impl.JwtTokenServiceImpl;
import com.prati.projetomercado.service.impl.UserDetailsServiceImpl;
import com.prati.projetomercado.utils.TokenUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserAutenticationFilter extends OncePerRequestFilter {

    private final JwtTokenServiceImpl jwtTokenService;
    private final UserDetailsServiceImpl userDetailsService;
    private final AccessTokenRepository accessTokenRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return Arrays.stream(SecurityConfiguration.PUBLIC_ENDPOINTS).anyMatch(
                pattern -> PathPatternRequestMatcher.withDefaults().matcher(pattern).matches(request)
        );
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1) Extrair o token do header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // sem token → segue a cadeia sem autenticar aqui
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring("Bearer ".length()).trim();

        try {
            // 2) Validar o token com o serviço correto
            var accessToken = accessTokenRepository.findByToken(token).orElse(null);
            if (accessToken == null) {
                throw new BadCredentialsException("Token inválido");
            }

            // 3) Checar expiração (o seu campo na tabela é expired_date, então o getter costuma ser getExpiredDate())
            var expiresAt = accessToken.getExpiredDate(); // se o nome for outro (getExpiresAt), troque aqui
            if (expiresAt != null && expiresAt.isBefore(Instant.now())) {
                throw new BadCredentialsException("Token expirado");
            }

            // 4) Montar Authentication no contexto (ajuste authorities conforme seu domínio)
            var user = accessToken.getAuthUser(); // pegue o usuário ligado ao token
            var authentication = new UsernamePasswordAuthenticationToken(user, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 5) Segue o fluxo
            filterChain.doFilter(request, response);

        } catch (BadCredentialsException e) {
            // Deixa o EntryPoint da SecurityConfiguration devolver o JSON 401
            throw e;

        } catch (RuntimeException e) {
            // Qualquer falha inesperada no processo de autenticação também vira 401
            String msg = (e.getMessage() != null && !e.getMessage().isBlank())
                    ? e.getMessage() : "Falha na autenticação";
            throw new BadCredentialsException(msg, e);
        }
    }
}
