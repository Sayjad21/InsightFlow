package com.insightflow.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey() {
        // Convert the secret string into a key suitable for HMAC-SHA algorithms
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        System.out.println("JWT Generation - Current time: " + now);
        System.out.println("JWT Generation - Expiry time: " + expiryDate);
        System.out.println("JWT Generation - Expiration milliseconds: " + expiration);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey()) // Uses the key directly, no need for SignatureAlgorithm in recent versions
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            boolean usernameMatches = extractedUsername.equals(username);
            boolean tokenNotExpired = !isTokenExpired(token);

            System.out.println("JWT Validation - Username from token: " + extractedUsername);
            System.out.println("JWT Validation - Expected username: " + username);
            System.out.println("JWT Validation - Username matches: " + usernameMatches);
            System.out.println("JWT Validation - Token not expired: " + tokenNotExpired);

            return usernameMatches && tokenNotExpired;
        } catch (Exception e) {
            System.out.println("JWT Validation - Error: " + e.getMessage());
            return false;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // Use verifyWith for the key in recent versions
                .build()
                .parseSignedClaims(token) // Use parseSignedClaims for JWS (Signed JWT)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        Date now = new Date();
        boolean expired = expiration.before(now);

        System.out.println("JWT Expiration Check - Token expires at: " + expiration);
        System.out.println("JWT Expiration Check - Current time: " + now);
        System.out.println("JWT Expiration Check - Is expired: " + expired);

        return expired;
    }
}