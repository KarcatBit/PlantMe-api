package com.grupo8.plantme_api.model.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PlantaResponseDTO {
    private Long id;
    private String nombre;
    private String speciesKey;
    private Integer frecuenciaDias; // Útil si el móvil quiere mostrar "Se riega cada X días"
    private LocalDateTime ultimoRiego;
    private LocalDateTime siguienteRiego; // ¡Calculado por el Backend!
}