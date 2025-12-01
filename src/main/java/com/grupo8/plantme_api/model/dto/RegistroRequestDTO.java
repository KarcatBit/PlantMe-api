package com.grupo8.plantme_api.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// Nota: No lleva @Entity ni @Id, es solo para transportar datos.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroRequestDTO {
    private String username;
    private String email;
    private String password;
}