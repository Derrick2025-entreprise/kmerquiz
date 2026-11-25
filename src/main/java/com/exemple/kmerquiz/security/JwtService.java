package com.exemple.kmerquiz.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    public static final String CLAIM_TYPE = "type";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private final Key signingKey;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration}") long accessExpirationMs,
                      @Value("${app.jwt.refreshExpiration}") long refreshExpirationMs) {
        this.signingKey = buildKey(secret);
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    private static Key buildKey(String secret) {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateAccessToken(UserPrincipal user) {
        return buildToken(user, TYPE_ACCESS, accessExpirationMs);
    }

    public String generateRefreshToken(UserPrincipal user) {
        return buildToken(user, TYPE_REFRESH, refreshExpirationMs);
    }

    private String buildToken(UserPrincipal user, String type, long expirationMs) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(user.getUsername())
                .addClaims(Map.of(
                        CLAIM_TYPE, type,
                        "uid", user.getIdUser(),
                        "role", user.getRoleUser().name()))
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }


    public boolean validateToken(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // signature invalide, jeton expiré, malformé ou vide
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return parse(token).getSubject();
    }


    public boolean isAccessToken(String token) {
        return TYPE_ACCESS.equals(parse(token).get(CLAIM_TYPE, String.class));
    }


    public boolean isRefreshToken(String token) {
        return TYPE_REFRESH.equals(parse(token).get(CLAIM_TYPE, String.class));
    }


    private Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public long getAccessExpirationMs() {
        return accessExpirationMs;
    }
}
