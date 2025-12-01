package com.grupo8.plantme_api.repository;


import com.grupo8.plantme_api.model.PlantaEntity;
import com.grupo8.plantme_api.model.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlantaRepository extends JpaRepository<PlantaEntity, Long> {
    // Para listar las plantas de un usuario concreto
    List<PlantaEntity> findAllByUsuario(UsuarioEntity usuario);
}