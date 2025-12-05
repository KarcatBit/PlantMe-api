
package com.grupo8.plantme_api.service;

import com.grupo8.plantme_api.model.EspecieEntity;
import com.grupo8.plantme_api.model.PlantaEntity;
import com.grupo8.plantme_api.model.UsuarioEntity;
import com.grupo8.plantme_api.model.dto.PlantaRequestDTO;
import com.grupo8.plantme_api.model.dto.PlantaResponseDTO;
import com.grupo8.plantme_api.repository.EspecieRepository; // Nueva Importación
import com.grupo8.plantme_api.repository.PlantaRepository;
import com.grupo8.plantme_api.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import org.apache.el.stream.Optional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
        LocalDateTime ahora = LocalDateTime.now(ZoneId.of("UTC"));
        planta.setUltimoRiego(ahora);
        planta.setSiguienteRiego(ahora.plusDays(frecuenciaDias));
        
        // 3. Guardar el cambio y devolver el DTO actualizado
        PlantaEntity plantaActualizada = plantaRepository.save(planta);
        return toResponseDTO(plantaActualizada);
    }       
    // --- 4. ACTUALIZAR PLANTA (EDITAR) ---
    @Transactional
    public PlantaResponseDTO actualizarPlanta(Long plantaId, PlantaRequestDTO request, String tokenUser) {
        PlantaEntity planta = plantaRepository.findById(plantaId)
                .orElseThrow(() -> new IllegalArgumentException("Planta no encontrada"));

        // VALIDACIÓN DE SEGURIDAD ROBUSTA (Igual que en regar)
        boolean coincideEmail = planta.getUsuario().getEmail().equalsIgnoreCase(tokenUser);
        boolean coincideUser = planta.getUsuario().getUsername().equalsIgnoreCase(tokenUser);

        if (!coincideEmail && !coincideUser) {
            System.out.println("❌ SEGURIDAD EDITAR: Token (" + tokenUser + ") no coincide con dueño (" + planta.getUsuario().getEmail() + ")");
            throw new SecurityException("Acceso denegado. No es tu planta.");
        }

        // Actualizar datos
        EspecieEntity nuevaEspecie = especieRepository.findByNombre(request.getSpeciesKey())
                .orElseThrow(() -> new IllegalArgumentException("Especie desconocida"));

        planta.setNombre(request.getNombre());
        planta.setEspecie(nuevaEspecie);
        planta.setUltimoRiego(request.getUltimoRiego());

        // Recalcular
        planta.setSiguienteRiego(request.getUltimoRiego().plusDays(nuevaEspecie.getFrecuenciaRiegoDias()));

        return toResponseDTO(plantaRepository.save(planta));
    }
    

    // --- 4. OBTENER PLANTA POR ID (para el regarPlanta) ---
    public PlantaEntity obtenerPlantaPorId(Long id) {
        return plantaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Planta no encontrada"));
    }

    // --- 5. ELIMINAR PLANTA ---
    @Transactional
    public void eliminarPlanta(Long plantaId, String tokenUser) {
        PlantaEntity planta = plantaRepository.findById(plantaId)
                .orElseThrow(() -> new IllegalArgumentException("Planta no encontrada con ID: " + plantaId));

        // VALIDACIÓN DE SEGURIDAD ROBUSTA
        // Comparamos si el token coincide con el Email O con el Username del dueño
        boolean coincideEmail = planta.getUsuario().getEmail().equalsIgnoreCase(tokenUser);
        boolean coincideUser = planta.getUsuario().getUsername().equalsIgnoreCase(tokenUser);

        if (!coincideEmail && !coincideUser) {
            System.out.println("❌ SEGURIDAD ELIMINAR: Token (" + tokenUser + ") no coincide con dueño (" + planta.getUsuario().getEmail() + ")");
            throw new SecurityException("Acceso denegado. No puedes eliminar esta planta.");
        }

        // Si la seguridad pasa, procedemos a eliminar
        plantaRepository.delete(planta);
    }
}