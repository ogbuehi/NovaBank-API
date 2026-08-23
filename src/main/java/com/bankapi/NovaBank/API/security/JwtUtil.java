package com.bankapi.NovaBank.API.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;


@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private Key signingKey;

    @PostConstruct
    public void init() {
        if(secret == null || secret.isBlank()){
            throw new IllegalStateException("JWT secret not configured");
        }

        signingKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );    }

    /**
     * Generate JWT Token
     */
    public String generateToken(String email, String role) {

        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + expiration)
                )
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extract Email (Subject)
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract Role
     */
    public String extractRole(String token) {
        return extractAllClaims(token)
                .get("role", String.class);
    }

    /**
     * Extract Expiration Date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic Claim Extractor
     */
    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {

        try{

            return claimsResolver.apply(extractAllClaims(token));
        }
        catch (JwtException ex){
            throw new BadCredentialsException("Invalid JWT");
        }
    }

    /**
     * Extract All Claims
     */
    public Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Check if Token Expired
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractAllClaims(token)
                    .getExpiration()
                    .before(new Date());
        } catch (ExpiredJwtException ex) {
            // JJWT rejects expired tokens while parsing; that means the token
            // is expired rather than malformed.
            return true;
        }
    }

    /**
     * Validate Token
     */
    public boolean isTokenValid(
            String token,
            String email
    ) {

        String extractedEmail = extractEmail(token);

        return extractedEmail.equals(email)
                && !isTokenExpired(token);
    }

    /**
     * Return Signing Key
     */
    public Key getSigningKey() {
        return signingKey;
    }
}
