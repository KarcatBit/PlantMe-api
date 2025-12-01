package com.grupo8.plantme_api.service;


import com.grupo8.plantme_api.model.UsuarioEntity;
import com.grupo8.plantme_api.model.dto.LoginRequestDTO;
import com.grupo8.plantme_api.model.dto.RegistroRequestDTO;
import com.grupo8.plantme_api.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service // 1. Marca esta clase como un Service de Spring
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder; // 2. Para encriptar contraseñas

    // 3. Constructor para Inyección de Dependencias
    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional // Asegura que la operación sea atómica (éxito o fallo total)
    public UsuarioEntity registrarUsuario(RegistroRequestDTO request) {
        
        // 4. Validar si el usuario o email ya existen
        if (usuarioRepository.findByUsername(request.getUsername()).isPresent() ||
            usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            
            // Aquí podrías lanzar una excepción personalizada (ej: IllegalArgumentException)
            throw new IllegalArgumentException("El usuario o correo ya está registrado.");
        }

        // 5. Crear la Entidad desde el DTO
        UsuarioEntity nuevoUsuario = UsuarioEntity.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                // 6. ENCRIPTAR la contraseña antes de guardarla
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        return usuarioRepository.save(nuevoUsuario);
    }
    
    // Método simple para verificar credenciales (la base para el login)
    public boolean verificarCredenciales(LoginRequestDTO request) {
        
        // 1. BUSCAR por Email (asumimos que request.getUsername() contiene el email)
        Optional<UsuarioEntity> usuarioOpt = usuarioRepository.findByEmail(request.getEmail()); 

        if (usuarioOpt.isEmpty()) {
            return false; // Email no encontrado
        }
        
        UsuarioEntity usuario = usuarioOpt.get();
        
        // 2. Comparar la contraseña ingresada con la encriptada en la BD
        return passwordEncoder.matches(request.getPassword(), usuario.getPassword());
    }
}