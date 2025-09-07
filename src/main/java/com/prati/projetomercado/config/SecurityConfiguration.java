package com.prati.projetomercado.config;

import com.prati.projetomercado.filter.UserAutenticationFilter;
import com.prati.projetomercado.service.impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    
    public static final String[] PUBLIC_ENDPOINTS = {
            "/auth/register",
            "/auth/register/",
            "/auth/login",
            "/auth/login/",
            "/auth/refresh-token",
            "/auth/refresh-token/",
            "/h2-console/**",
            "/h2-console/",
            "/h2-console"
    };
    
    public static final String[] AUTH_REQUIRED_ENDPOINTS = {
            "/auth/test-autenticated",
            "/api/nfce/scrape"
    };

    @Autowired
    private UserAutenticationFilter userAutenticationFilter;

    @Bean
    @Order(10)
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .headers(HeadersConfigurer::disable)
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        authorize -> authorize
                                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                                .requestMatchers(AUTH_REQUIRED_ENDPOINTS).authenticated()
                                .anyRequest().denyAll()
                ).addFilterBefore(userAutenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();

    }

    @Bean
    public FilterRegistrationBean<UserAutenticationFilter> userAutenticationFilterFilterRegistrationBean() {
        var filter = new FilterRegistrationBean<UserAutenticationFilter>();
        filter.setFilter(userAutenticationFilter);
        filter.addUrlPatterns(AUTH_REQUIRED_ENDPOINTS);
        return filter;
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
    public DaoAuthenticationProvider authenticationProvider(
            UserDetailsServiceImpl userDetailsService, PasswordEncoder passwordEncoder
    ) {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
            
}
