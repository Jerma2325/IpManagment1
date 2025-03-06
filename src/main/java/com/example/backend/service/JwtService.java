package com.example.backend.service;

import com.example.backend.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
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
        try {
            String username = extractClaim(token, Claims::getSubject);
            return username;
        } catch (Exception e) {
            System.err.println("Error extracting username: " + e.getMessage());
            throw e;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        String token = Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();

        System.out.println("Generated token for user: " + user.getUsername());
        return token;
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
        Date issuedAt = new Date(System.currentTimeMillis());
        Date expiration = new Date(System.currentTimeMillis() + jwtExpiration);

        System.out.println("Creating token for subject: " + subject);
        System.out.println("Token issuedAt: " + issuedAt);
        System.out.println("Token expiration: " + expiration);

        String token = Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();

        return token;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean isValid = (username.equals(userDetails.getUsername())) && !isTokenExpired(token);

            if (!isValid) {
                if (!username.equals(userDetails.getUsername())) {
                    System.err.println("Token username (" + username + ") doesn't match UserDetails username (" +
                            userDetails.getUsername() + ")");
                }
                if (isTokenExpired(token)) {
                    System.err.println("Token is expired");
                }
            }

            return isValid;
        } catch (Exception e) {
            System.err.println("Error validating token: " + e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        boolean isExpired = expiration.before(new Date());

        if (isExpired) {
            System.out.println("Token expired at: " + expiration + ", current time: " + new Date());
        }

        return isExpired;
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
            cachedSecretKey = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS512.getJcaName());
        }
        return cachedSecretKey;
    }

    public String getUsernameFromToken(String token) {
        return extractUsername(token);
    }
}