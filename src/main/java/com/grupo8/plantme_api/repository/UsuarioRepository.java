package com.grupo8.plantme_api.repository;

import com.grupo8.plantme_api.model.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository toma dos parámetros: <La Entidad, El tipo de la clave primaria (ID)>
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {
    
    // Spring Data JPA crea este método por nosotros. Lo usaremos para el login.
    Optional<UsuarioEntity> findByUsername(String username);

    // Opcional: También podrías necesitar buscar por email para el registro
    Optional<UsuarioEntity> findByEmail(String email);
}
