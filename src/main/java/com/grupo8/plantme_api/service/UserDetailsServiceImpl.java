package com.grupo8.plantme_api.service;

import com.grupo8.plantme_api.model.UsuarioEntity;
import com.grupo8.plantme_api.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service // Marca esta clase como un servicio
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    // Inyección del Repositorio
    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Implementación del método que Spring Security llama para cargar un usuario.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        
        // El parámetro String username de la interfaz ahora será interpretado como el EMAIL
        UsuarioEntity usuario = usuarioRepository.findByEmail(email) // <--- ¡CAMBIO AQUÍ!
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));
        
        return usuario;
    }
}
