package com.grupo8.plantme_api.config;

import com.grupo8.plantme_api.service.JwtService;
import com.grupo8.plantme_api.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.RequiredArgsConstructor;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
         HttpServletRequest request, 
         HttpServletResponse response, 
         FilterChain filterChain 
    ) throws ServletException, IOException {
        
        // 1. Verificar si hay un token en el encabezado
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Si no hay token o no tiene el formato correcto, sigue con la cadena de filtros
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extraer el token y el email (username)
        jwt = authHeader.substring(7); // Quita "Bearer "
        userEmail = jwtService.extractUsername(jwt); // Extrae el email del token

        // 3. Validar el token
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Cargar los detalles del usuario de la BD
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            
            // Verificar si el token es válido (no expirado y firma correcta)
            if (jwtService.isTokenValid(jwt, userDetails)) {

                    var authorities = userDetails.getAuthorities();
                    
                    // --- DEBUG: Ver qué roles tiene ---
                    System.out.println(" Roles del usuario: " + authorities);
                    
                    // Si es válido, crear un objeto de autenticación
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities() // <--- ¡Importante! Aquí asignamos los roles.
                    );
                    authToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    // 4. ESTABLECER LA AUTENTICACIÓN EN EL CONTEXTO DE SEGURIDAD
                    SecurityContextHolder.getContext().setAuthentication(authToken); // <--- ¡CLAVE!
                    System.out.println(" Contexto de seguridad establecido para: " + userEmail);
                }
            }
            
            // 5. Continuar el flujo de la petición
            filterChain.doFilter(request, response);
    }
}
