
package com.grupo8.plantme_api.service;

import com.grupo8.plantme_api.model.EspecieEntity;
import com.grupo8.plantme_api.model.PlantaEntity;
import com.grupo8.plantme_api.model.UsuarioEntity;
import com.grupo8.plantme_api.model.dto.PlantaRequestDTO;
import com.grupo8.plantme_api.model.dto.PlantaResponseDTO;
import com.grupo8.plantme_api.repository.EspecieRepository; // Nueva Importación
import com.grupo8.plantme_api.repository.PlantaRepository;
import com.grupo8.plantme_api.repository.UsuarioRepository;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors; // Para mapear listas

@Service
public class PlantaService {

    private final PlantaRepository plantaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EspecieRepository especieRepository; // Nuevo

    public PlantaService(PlantaRepository plantaRepository, UsuarioRepository usuarioRepository, EspecieRepository especieRepository) {
        this.plantaRepository = plantaRepository;
        this.usuarioRepository = usuarioRepository;
        this.especieRepository = especieRepository;
    }

    // --- UTILITY: Convierte Entity a ResponseDTO ---
    private PlantaResponseDTO toResponseDTO(PlantaEntity entity) {
        // Calcula la siguiente fecha de riego
        return PlantaResponseDTO.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .speciesKey(entity.getEspecie().getNombre()) // El nombre de la especie es el speciesKey
                .frecuenciaDias(entity.getEspecie().getFrecuenciaRiegoDias())
                .ultimoRiego(entity.getUltimoRiego())
                .siguienteRiego(entity.getSiguienteRiego()) // Lo que está guardado en la BD
                .build();
    }

    // --- 1. CREAR PLANTA ---
    public PlantaResponseDTO crearPlanta(PlantaRequestDTO request, String userEmail) {
        
        UsuarioEntity usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + userEmail));
        
        // 1. Encontrar la especie por el speciesKey enviado desde Android
        EspecieEntity especie = especieRepository.findByNombre(request.getSpeciesKey())
                .orElseThrow(() -> new IllegalArgumentException("Especie no encontrada: " + request.getSpeciesKey()));
        
        // 2. CALCULAR el siguiente riego
        LocalDateTime siguienteRiego = request.getUltimoRiego()
                .plusDays(especie.getFrecuenciaRiegoDias());
        
        // 3. Mapear y guardar
        PlantaEntity nuevaPlanta = PlantaEntity.builder()
                .nombre(request.getNombre())
                .especie(especie) // Asigna la entidad Especie
                .usuario(usuario)
                .ultimoRiego(request.getUltimoRiego())
                .siguienteRiego(siguienteRiego) // Guarda la fecha calculada
                .build();
        
        PlantaEntity plantaGuardada = plantaRepository.save(nuevaPlanta);
        
        // 4. Devolver en formato DTO
        return toResponseDTO(plantaGuardada);
    }
    
    // --- 2. LISTAR PLANTAS ---
    public List<PlantaResponseDTO> obtenerPlantasPorUsuario(String userEmail) {
        UsuarioEntity usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + userEmail));
        
        List<PlantaEntity> plantas = plantaRepository.findAllByUsuario(usuario);
        
        // Mapear la lista de entidades a la lista de DTOs
        return plantas.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // --- 3. REINICIAR RIEGO (Actualizar la fecha, como en el botón "regar") ---
    public PlantaResponseDTO regarPlanta(Long plantaId, String userEmail) {
        
        PlantaEntity planta = plantaRepository.findById(plantaId)
            .orElseThrow(() -> new IllegalArgumentException("Planta no encontrada con ID: " + plantaId));

        // Verificar que la planta pertenezca al usuario logueado (Seguridad)
        if (!planta.getUsuario().getEmail().equals(userEmail)) {
            throw new SecurityException("Acceso denegado. Esta planta no pertenece al usuario.");
        }
        
        // 1. Obtener la frecuencia de la especie
        Integer frecuenciaDias = planta.getEspecie().getFrecuenciaRiegoDias();
        
        // 2. Actualizar último riego a HOY y calcular el nuevo siguiente riego
        LocalDateTime ahora = LocalDateTime.now();
        planta.setUltimoRiego(ahora);
        planta.setSiguienteRiego(ahora.plusDays(frecuenciaDias));
        
        // 3. Guardar el cambio y devolver el DTO actualizado
        PlantaEntity plantaActualizada = plantaRepository.save(planta);
        return toResponseDTO(plantaActualizada);
    }       
    @Transactional
    public PlantaResponseDTO actualizarPlanta(Long plantaId, PlantaRequestDTO request, String userEmail) {
        PlantaEntity planta = plantaRepository.findById(plantaId)
                .orElseThrow(() -> new IllegalArgumentException("Planta no encontrada con ID: " + plantaId));

        // --- DEPURACIÓN: Comprobar qué emails se están comparando ---
        // (Puedes borrar estas líneas después de que funcione)
        System.out.println("Planta Owner Email (DB): " + planta.getUsuario().getEmail());
        System.out.println("Authenticated User Email (Token): " + userEmail);

        // 1. Verificar Seguridad: La planta debe pertenecer al usuario logueado
        // CORRECCIÓN: Usar trim() para eliminar espacios y equalsIgnoreCase() para ignorar mayúsculas/minúsculas
        if (!planta.getUsuario().getEmail().trim().equalsIgnoreCase(userEmail.trim())) {
            throw new SecurityException("Acceso denegado. Esta planta no pertenece al usuario.");
        }
        
        // 2. Actualizar Especie (si cambió)
        EspecieEntity nuevaEspecie = especieRepository.findByNombre(request.getSpeciesKey())
                .orElseThrow(() -> new IllegalArgumentException("Especie no encontrada: " + request.getSpeciesKey()));
        
        // 3. Actualizar campos
        planta.setNombre(request.getNombre());
        planta.setEspecie(nuevaEspecie);
        planta.setUltimoRiego(request.getUltimoRiego()); // El cliente envía la fecha de último riego modificada
        
        // 4. Recalcular el siguiente riego (basado en la nueva especie y el nuevo último riego)
        // Nota: El cliente (app) debería enviar el último riego en formato LocalDateTime.
        LocalDateTime siguienteRiego = request.getUltimoRiego()
                .plusDays(nuevaEspecie.getFrecuenciaRiegoDias());
        planta.setSiguienteRiego(siguienteRiego);

        // 5. Guardar el cambio y devolver el DTO actualizado
        PlantaEntity plantaActualizada = plantaRepository.save(planta);
        return toResponseDTO(plantaActualizada);
    }

    // --- 4. OBTENER PLANTA POR ID (para el regarPlanta) ---
    public PlantaEntity obtenerPlantaPorId(Long id) {
        return plantaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Planta no encontrada"));
    }
}