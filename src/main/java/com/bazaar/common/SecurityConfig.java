package com.bazaar.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * TEMPORAL: mientras el módulo de Auth (RF-03) no esté implementado,
 * dejamos todas las rutas abiertas para poder probar los demás módulos
 * (Direcciones, Categorías, etc.) sin necesidad de login.
 *
 * Cuando se implemente JWT + roles, esta clase se reemplaza por la
 * configuración real (rutas públicas vs protegidas, filtro JWT, etc.).
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}
