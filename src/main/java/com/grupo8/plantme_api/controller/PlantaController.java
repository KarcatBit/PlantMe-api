package com.grupo8.plantme_api.controller;

import com.grupo8.plantme_api.model.dto.PlantaRequestDTO;
import com.grupo8.plantme_api.model.dto.PlantaResponseDTO;
import com.grupo8.plantme_api.service.PlantaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plantas") // Define la ruta base para todas las peticiones de esta clase
public class PlantaController {

    private final PlantaService plantaService;

    // Inyección del servicio
    public PlantaController(PlantaService plantaService) {
        this.plantaService = plantaService;
    }

    /**
     * Método auxiliar para sacar el email del token JWT que ya fue validado.
     * Así sabemos qué usuario está haciendo la petición.
     */
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Verificamos si el 'principal' (el usuario) es nuestra entidad UsuarioEntity
        if (authentication.getPrincipal() instanceof com.grupo8.plantme_api.model.UsuarioEntity) {
            // Hacemos casting y sacamos el email directamente
            return ((com.grupo8.plantme_api.model.UsuarioEntity) authentication.getPrincipal()).getEmail();
        }
        
        // Fallback por si acaso
        return authentication.getName();
    }

    // --- 1. CREAR UNA PLANTA (POST) ---
    @PostMapping
    public ResponseEntity<PlantaResponseDTO> crearPlanta(@RequestBody PlantaRequestDTO request) {
        System.out.println("🌱 Recibida petición para crear planta: " + request.getNombre());
        
        try {
            String userEmail = getCurrentUserEmail(); // Obtenemos el email del token
            PlantaResponseDTO nuevaPlanta = plantaService.crearPlanta(request, userEmail);
            return new ResponseEntity<>(nuevaPlanta, HttpStatus.CREATED); // 201 Created
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error en consola si falla
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // --- 2. LISTAR MIS PLANTAS (GET) ---
    @GetMapping
    public ResponseEntity<List<PlantaResponseDTO>> listarMisPlantas() {
        String userEmail = getCurrentUserEmail();
        List<PlantaResponseDTO> plantas = plantaService.obtenerPlantasPorUsuario(userEmail);
        return ResponseEntity.ok(plantas); // 200 OK
    }
    
    // --- 3. REGAR PLANTA (PUT) ---
    // Este es el botón de la gotita 💧
    @PutMapping("/{id}/regar")
    public ResponseEntity<PlantaResponseDTO> regarPlanta(@PathVariable Long id) {
        try {
            String userEmail = getCurrentUserEmail();
            PlantaResponseDTO plantaActualizada = plantaService.regarPlanta(id, userEmail);
            return ResponseEntity.ok(plantaActualizada);
        } catch (SecurityException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // No es tu planta
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Planta no existe
        }
    }
}