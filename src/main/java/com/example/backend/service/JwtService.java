package com.example.backend.service;

import com.example.backend.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey cachedSecretKey;

    public String extractUsername(String token) {
        System.out.println(token);
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return createToken(extraClaims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        String token = Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();

        System.out.println("Generated token with subject: " + subject); // For debugging
        return token;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (Exception e) {
            System.err.println("Token validation error: " + e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        try {
            JwtParser parser = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build();


            return parser.parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            System.err.println("Error parsing JWT token: " + e.getMessage());
            throw e;
        }
    }

    private SecretKey getSigningKey() {
        if (cachedSecretKey == null) {
            byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            cachedSecretKey = Keys.hmacShaKeyFor(keyBytes);
        }
        return cachedSecretKey;
    }
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey).build()
                .parseSignedClaims(token)
                .getBody()
                .getSubject();
    }
}