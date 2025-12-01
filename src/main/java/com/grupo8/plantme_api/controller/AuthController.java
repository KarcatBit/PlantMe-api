package com.grupo8.plantme_api.controller;
import com.grupo8.plantme_api.model.dto.JwtResponseDTO;
import com.grupo8.plantme_api.model.dto.LoginRequestDTO;
import com.grupo8.plantme_api.model.dto.RegistroRequestDTO;
import com.grupo8.plantme_api.service.AuthService;
import com.grupo8.plantme_api.service.JwtService; // Importación nueva
import com.grupo8.plantme_api.service.UserDetailsServiceImpl; // Importación nueva

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager; // Importación nueva
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Importación nueva
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") 
public class AuthController {
    
    // Inyección de dependencias de servicios que ya tenías
    private final AuthService authService;
    private final UserDetailsServiceImpl userDetailsService;

    // Servicios nuevos para JWT
    private final JwtService jwtService; 
    private final AuthenticationManager authenticationManager; // Necesario para verificar credenciales

    // Constructor para Inyección de Dependencias
    public AuthController(
            AuthService authService,
            JwtService jwtService,
            UserDetailsServiceImpl userDetailsService,
            AuthenticationManager authenticationManager 
    ) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
    }
    
    // El método de registro se mantiene igual
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegistroRequestDTO request) {
        try {
            authService.registrarUsuario(request);
            return new ResponseEntity<>("Usuario registrado exitosamente.", HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>("Error al intentar registrar el usuario.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

   
    @PostMapping("/login") 
    public ResponseEntity<JwtResponseDTO> login(@RequestBody LoginRequestDTO request) {
        
        // 1. Intentar autenticar credenciales usando el AuthenticationManager
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(), // Identificador (email)
                    request.getPassword() // Contraseña
                )
            );
        // AuthController.java - Dentro del método login
        } catch (Exception e) {
            // Devuelve 401 Unauthorized sin cuerpo (body)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); 
        }

        // 2. Si la autenticación fue exitosa:
        
        // Cargar los detalles del usuario desde la BD
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        
        // Generar el token JWT
        final String jwt = jwtService.generateToken(userDetails);
        
        // Devolver el Token JWT al móvil en el formato JwtResponseDTO
        return ResponseEntity.ok(JwtResponseDTO.builder().token(jwt).build());
    }
}