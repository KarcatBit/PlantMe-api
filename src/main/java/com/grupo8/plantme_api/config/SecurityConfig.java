package com.grupo8.plantme_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. Define el encriptador de contraseñas (necesario para el AuthService)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    // 2. Define la cadena de filtros de seguridad.
   @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ... (Configuración de csrf y sessionManagement)
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            .authorizeHttpRequests(auth -> auth
                // ¡ESTA LÍNEA ES CLAVE! Debe permitir el acceso a CUALQUIER COSA
                // que empiece con /api/auth/ (login, register, etc.)
                .requestMatchers("/api/auth/**").permitAll() 
                
                // Todas las demás peticiones deben estar autenticadas (protegidas por JWT)
                .anyRequest().authenticated()
            );

        return http.build();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    // Opcional: Si el error persiste, a veces hay que definir el AuthenticationManager
    // Aunque Spring Boot 3+ lo suele hacer automáticamente si tienes los beans.
}