package com.grupo8.plantme_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import lombok.NoArgsConstructor;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 

public class UsuarioEntity implements UserDetails { // Implementa UserDetails para Spring Security

    @Id // Clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Valor auto-incremental (MySQL/PostgreSQL)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username; // Se usará para el login (debe ser único)

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // ¡Aquí se guarda la contraseña ENCRIPTADA!

    // --- Relación con Plantas ---
    
    // Mapeo: Un usuario tiene Muchas plantas (OneToMany)
    // 'mappedBy="usuario"' indica que la clave foránea está en la entidad Planta.
    // 'CascadeType.ALL' significa que si borras un usuario, se borran sus plantas.
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PlantaEntity> plantas; 
    
    // ----------------------------------------------------------------------
    // --- Implementación de Spring Security (UserDetails) ---
    // ----------------------------------------------------------------------

    /**
     * Define los permisos o roles del usuario.
     * En este proyecto simple, devolvemos una lista vacía o un rol básico.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Podrías devolver List.of(new SimpleGrantedAuthority("ROLE_USER")); 
        return List.of(); 
    }

    // Estos métodos simplemente indican que la cuenta está activa. 
    // Los dejamos en 'true' para el proyecto:

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}