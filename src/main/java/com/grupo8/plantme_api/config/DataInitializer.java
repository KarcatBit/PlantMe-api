package com.grupo8.plantme_api.config;

import com.grupo8.plantme_api.model.EspecieEntity;
import com.grupo8.plantme_api.repository.EspecieRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final EspecieRepository especieRepository;

    public DataInitializer(EspecieRepository especieRepository) {
        this.especieRepository = especieRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Solo insertamos si la tabla está vacía para no duplicar
        if (especieRepository.count() == 0) {
            System.out.println("🌱 Cargando especies iniciales...");
            
            // Ajusta estos días según tu lógica de Android (SpeciesDefault)
            crearEspecie("cactus", 30);     
            crearEspecie("photos", 7);
            crearEspecie("aloe", 14);
            crearEspecie("sansevieria", 18);
            crearEspecie("suculenta", 21);
            crearEspecie("test", 1);
            
            // Añade aquí todas las que tengas en tu app móvil
        }
    }

    private void crearEspecie(String nombre, int dias) {
        EspecieEntity especie = EspecieEntity.builder()
                .nombre(nombre)
                .frecuenciaRiegoDias(dias)
                .build();
        especieRepository.save(especie);
    }
}