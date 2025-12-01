package com.grupo8.plantme_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plantas")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder

public class PlantaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre; // Ej: "Mi Cactus Favorito"

    // --- RELACIÓN CON ESPECIE ---
    // En lugar de guardar el texto, guardamos el ID de la especie
    @ManyToOne 
    @JoinColumn(name = "especie_id", nullable = false)
    private EspeciesEntity especie; 

    // --- RELACIÓN CON USUARIO ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    // --- FECHAS DE RIEGO ---
    @Column(nullable = false)
    private LocalDateTime ultimoRiego; // Cuándo la regaste la última vez

    @Column(nullable = false)
    private LocalDateTime siguienteRiego; // Backend calculará esto automáticamente
}