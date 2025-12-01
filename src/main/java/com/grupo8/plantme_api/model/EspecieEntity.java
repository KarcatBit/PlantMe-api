package com.grupo8.plantme_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "especies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EspecieEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nombre; // Ej: "Cactus", "Suculenta", "Flor"

    @Column(nullable = false)
    private Integer frecuenciaRiegoDias; // Ej: 7, 15, 3
}