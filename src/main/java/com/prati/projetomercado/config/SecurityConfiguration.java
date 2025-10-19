package com.prati.projetomercado.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prati.projetomercado.exceptions.ErrorResponse;
import com.prati.projetomercado.filter.UserAutenticationFilter;
import com.prati.projetomercado.security.oauth2.CustomOAuth2UserService;
import com.prati.projetomercado.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.prati.projetomercado.security.oauth2.handlers.OAuth2AuthSuccessHandler;
import com.prati.projetomercado.service.impl.UserDetailsServiceImpl;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import org.springframework.http.HttpStatus;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    public static final String[] PUBLIC_ENDPOINTS = {
            "/auth/**",
            "/oauth2/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/h2-console/**"
    };

    private final UserAutenticationFilter userAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthSuccessHandler oAuth2AuthSuccessHandler;
    private final ObjectMapper objectMapper;

    public SecurityConfiguration(UserAutenticationFilter userAuthenticationFilter,
                             CustomOAuth2UserService customOAuth2UserService,
                             OAuth2AuthSuccessHandler oAuth2AuthSuccessHandler,
                             ObjectMapper objectMapper) {
    this.userAuthenticationFilter = userAuthenticationFilter;
    this.customOAuth2UserService = customOAuth2UserService;
    this.oAuth2AuthSuccessHandler = oAuth2AuthSuccessHandler;
    this.objectMapper = objectMapper;
}

    @Bean
    public HttpCookieOAuth2AuthorizationRequestRepository cookieOAuth2AuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    @Bean
    @Order(10)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**"), new AntPathRequestMatcher("/auth/**")))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(user -> user.userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthSuccessHandler)
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, authEx) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json;charset=UTF-8");
                            var body = objectMapper.writeValueAsString(
                                    new ErrorResponse(
                                            HttpStatus.UNAUTHORIZED,
                                            authEx.getMessage() != null ? authEx.getMessage() : "Token inválido ou expirado",
                                            req.getRequestURI()
                                    )
                            );
                            res.getWriter().write(body);
                        })
                        .accessDeniedHandler((req, res, denied) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType("application/json;charset=UTF-8");
                            var body = objectMapper.writeValueAsString(
                                    new ErrorResponse(
                                            HttpStatus.FORBIDDEN, // <--- 403 aqui
                                            denied.getMessage() != null ? denied.getMessage() : "Acesso negado",
                                            req.getRequestURI()
                                    )
                            );
                            res.getWriter().write(body);
                        })
                )

                .addFilterBefore(userAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("http://127.0.0.1:5173", "http://localhost:5173"));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsServiceImpl userDetailsService,
                                                            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}
