package com.grupo8.plantme_api.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PlantaRequestDTO {
    private String nombre;
    private String speciesKey; // Ej: "cactus" (debe coincidir con la BD)
    private LocalDateTime ultimoRiego; // Fecha/Hora que seleccionó el usuario
}