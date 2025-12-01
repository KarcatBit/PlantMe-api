package com.grupo8.plantme_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plantas")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PlantaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre; 

    private String tipo; 

    @Column(nullable = false)
    private Integer frecuenciaDias; 


    @Column(nullable = false)
    private LocalDateTime fechaUltimoRegado; 

    // --- Relación: N Plantas pertenecen a 1 Usuario ---
    
    // Esta anotación define la relación de muchos a uno (ManyToOne)
    // El 'fetch = FetchType.LAZY' significa que el usuario solo se carga si es necesario.
    @ManyToOne(fetch = FetchType.LAZY) 
    // Crea la columna 'usuario_id' en la tabla de plantas
    @JoinColumn(name = "usuario_id", nullable = false) 
    private UsuarioEntity usuario; 
    
}