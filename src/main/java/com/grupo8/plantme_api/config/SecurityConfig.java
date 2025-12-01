package com.grupo8.plantme_api.config;


import com.grupo8.plantme_api.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // Lombok genera el constructor para los campos final
public class SecurityConfig {

    // 1. INYECCIONES (Solo lo externo)
    // Solo inyectamos el Filtro y el Servicio de Usuarios.
    // NO inyectamos PasswordEncoder ni AuthProvider aquí para evitar ciclos.
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    // 2. FILTRO DE SEGURIDAD (El portero)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF para API REST
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Sin sesiones de servidor
            
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas (Login, Registro)
                .requestMatchers("/api/auth/**").permitAll() 
                // Todo lo demás requiere Token
                .anyRequest().authenticated()
            )
            // Añadir nuestro filtro JWT antes del filtro de usuario/pass estándar
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 3. PROVEEDOR DE AUTENTICACIÓN (El cerebro)
    // Conecta el UserDetailsService con el PasswordEncoder
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder()); // Usamos el método de abajo
        return authProvider;
    }

    // 4. MANAGER DE AUTENTICACIÓN (El jefe)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 5. ENCRIPTADOR DE CONTRASEÑAS (La herramienta)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}