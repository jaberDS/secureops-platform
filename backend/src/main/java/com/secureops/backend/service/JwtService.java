package com.secureops.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationTime;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-ms:3600000}") long expirationTime) {

        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "JWT secret must contain at least 32 characters"
            );
        }

        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        if (expirationTime <= 0) {
            throw new IllegalStateException(
                    "JWT expiration must be greater than zero"
            );
        }

        this.expirationTime = expirationTime;
    }

    public String generateToken(
            String email,
            String role) {

        Date now = new Date();

        Date expiration =
                new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public String extractEmail(String token) {

        return extractClaim(
                token,
                Claims::getSubject
        );
    }

    public boolean isTokenValid(
            String token,
            UserDetails userDetails) {

        String email = extractEmail(token);

        return email.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {

        return extractExpiration(token)
                .before(new Date());
    }

    private Date extractExpiration(String token) {

        return extractClaim(
                token,
                Claims::getExpiration
        );
    }

    private <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver) {

        Claims claims =
                Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

        return claimsResolver.apply(claims);
    }
}
