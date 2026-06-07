package com.eventflow.eventflow.component;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtils {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    @Value("${spring.security.jwt.secret-key}")
    private String secretKey;

    @Value("${spring.security.jwt.expiration-ms}")
    private long expirationMs;

    @Value("${spring.security.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(String email) {
        return generateAccessToken(email);
    }

    public String generateAccessToken(String email) {
        return Jwts.builder()
                .subject(email)
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSecretKey())
                .compact();
    }

    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .subject(email)
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(getSecretKey())
                .compact();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build().
                parseSignedClaims(token).
                getPayload().
                getSubject();
    }

    public boolean validateToken(String token) {
        return validateAccessToken(token);
    }

    public boolean validateAccessToken(String token) {
        String email = getEmailFromToken(token);
        return email != null && ACCESS_TOKEN_TYPE.equals(getTokenType(token)) && !isTokenExpired(token);
    }

    public boolean validateRefreshToken(String token) {
        String email = getEmailFromToken(token);
        return email != null && REFRESH_TOKEN_TYPE.equals(getTokenType(token)) && !isTokenExpired(token);
    }

    private String getTokenType(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get(TOKEN_TYPE_CLAIM, String.class);
    }

    private boolean isTokenExpired(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }
}
