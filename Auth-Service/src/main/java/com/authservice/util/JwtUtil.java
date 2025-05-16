package com.authservice.util;

import com.authservice.dto.UserDTO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "3c3b87c2e5cec7a0360db057225d0608f5b84c14830734efd7c4764adc5a6d147b7f05ad00d418110037e50aef72f7f8700a99238eb4c54dee2373c432c6c2dd3f8ca4a88e4184a548917b430f2c38a05f14a3d657f24277d9ddcded1d04234b5bbe6fceba4d0304d138d6a159209db4254e745f7e59464ba057b56e0dce994ca76176598a80f31302f6207c58d4f45e540cc9cede749a7907f926fb652936f1fdceb96f0220fb76b68593f3eae210f07c21a8cf9b340232fae196dd0a07eed641a568b0e0529212b67b3bcd394e52978557180eeea50f5542c18ab1ff3a909be30e6e21aa9f747d004eff6312a14f1002c4d0ab948ce712dbe21a5fcc99ef04"; // Replace with a securely generated key
    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    public String generateToken(UserDTO user) {
        return Jwts.builder()
                .claim("userId", user.getId().toString())
                .claim("email", user.getEmail())
                .claim("username", user.getUsername())
                .claim("roles", user.getRoles())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UserDTO user) {
        return Jwts.builder()
                .claim("userId", user.getId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }


    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("email", String.class);
    }

    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("username", String.class);
    }

    public Set<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return new HashSet<>((List<String>) claims.get("roles"));
    }

    public UUID extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return UUID.fromString(claims.get("userId", String.class));
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
