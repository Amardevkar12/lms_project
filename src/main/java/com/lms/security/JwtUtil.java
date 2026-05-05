package com.lms.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // =========================
    // EXTRACT USERNAME
    // =========================
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // =========================
    // EXTRACT EXPIRATION
    // =========================
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // =========================
    // GENERIC CLAIM EXTRACTOR
    // =========================
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // =========================
    // PARSE TOKEN
    // =========================
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // =========================
    // CHECK EXPIRY
    // =========================
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // =========================
    // GENERATE TOKEN
    // =========================
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // =========================
    // VALIDATE TOKEN
    // =========================
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            logger.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    // =========================
    // SECRET KEY HANDLING (FINAL FIX)
    // =========================
    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
}