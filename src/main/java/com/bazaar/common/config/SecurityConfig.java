package com.bazaar.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración TEMPORAL para desarrollo: abre todos los endpoints
 * mientras no tengas login/JWT implementados en "usuarios".
 *
 * IMPORTANTE: antes de producción, esto debe reemplazarse por una
 * configuración real con spring-boot-starter-security-oauth2-resource-server
 * validando JWT y restringiendo por rol (comprador, vendedor, admin).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // API stateless: no hay formularios ni sesión de navegador
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}