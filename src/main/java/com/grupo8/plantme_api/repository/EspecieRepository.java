package com.grupo8.plantme_api.repository;

import com.grupo8.plantme_api.model.EspecieEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EspecieRepository extends JpaRepository<EspecieEntity, Long> {
    // Nos servirá para buscar "Cactus" cuando nos llegue el dato del móvil
    Optional<EspecieEntity> findByNombre(String nombre);
}