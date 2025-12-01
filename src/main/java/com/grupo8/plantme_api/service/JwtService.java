package com.grupo8.plantme_api.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // 1. Inyecta la clave secreta (el valor de application.properties)
    @Value("${jwt.secret-key}")
    private String SECRET_KEY;

    // 2. Inyecta el tiempo de expiración (el valor de 10 años)
    @Value("${jwt.expiration-time}")
    private long EXPIRATION_TIME; 

    // ----------------- GENERACIÓN DEL TOKEN -----------------
    
    /**
     * Genera un token JWT a partir de los detalles del usuario.
     * @param userDetails Los detalles del usuario (nuestra UsuarioEntity)
     * @return El token JWT como String.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    private String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername()) // El identificador (email)
                .setIssuedAt(new Date(System.currentTimeMillis())) // Fecha de creación
                // 3. Usa la expiración de 10 años para la sesión persistente
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) 
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Convierte la clave secreta (String) a una clave de firma (Key).
     */
    private Key getSignInKey() {
        // La clave secreta debe ser decodificada de Base64
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    // ----------------- EXTRACCIÓN Y VALIDACIÓN (Para uso futuro) -----------------

    // Extrae el identificador del usuario (el 'subject', que es el email)
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    // Método completo para la validación del token (verifica firma y expiración)
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}