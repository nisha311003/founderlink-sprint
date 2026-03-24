package com.founderlink.apiGateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {


    @Value("${jwt.secret}")
    private String secret;

    public Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    public boolean validateToken(String token){
        try{
            extractAllClaims(token);
            return true;
        }catch(Exception e){
            System.out.println("JWT ERROR: " + e.getClass().getSimpleName());
            System.out.println("JWT MESSAGE: " + e.getMessage());
            return false;
        }
    }
    public String extractEmail(String token){
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token){
        return extractAllClaims(token).get("role", String.class);
    }

    public Long extractUserId(String token){
        return extractAllClaims(token).get("userId", Long.class);
    }

}
