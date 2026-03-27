package com.services.comedor.security;

import com.services.comedor.entity.Empleado;
import com.services.comedor.entity.Usuario;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // Esta clave debe ir en tu application.yml (mínimo 32 caracteres)
    @Value("${jwt.secret:tu_clave_secreta_super_segura_para_meyi_soft_2026_xyz}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // 1 día en milisegundos por defecto
    private int jwtExpirationInMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 🔥 Token para la App (Empleado)
    public String generateTokenEmpleado(Empleado empleado) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(empleado.getId().toString())
                .claim("rol", "ROLE_EMPLEADO")
                // .claim("comedorId", empleado.getComedor().getId())
                .claim("nombre", empleado.getNombre())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    // 🔥 Token para la Tablet (Staff)
    public String generateTokenStaff(Usuario usuario, Long comedorTabletId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(usuario.getId().toString())
                .claim("rol", usuario.getRol()) // ej: ROLE_CAJERO
                .claim("comedorActivoId", comedorTabletId)
                .claim("nombre", usuario.getNombre())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims getClaimsFromJWT(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            // Aquí podrías loguear el error (token expirado, alterado, etc.)
            return false;
        }
    }
}