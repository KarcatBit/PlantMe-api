package com.grupo8.plantme_api.repository;


import com.grupo8.plantme_api.model.PlantaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlantaRepository extends JpaRepository<PlantaEntity, Long> {

    // Este método es crucial: te permite obtener TODAS las plantas 
    // asociadas a un usuario específico (usando el ID del usuario).
    List<PlantaEntity> findByUsuarioId(Long usuarioId);
}