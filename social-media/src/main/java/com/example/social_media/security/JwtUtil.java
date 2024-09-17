package com.example.social_media.security;

import com.example.social_media.dto.UserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long expiration;

    // Method to generate JWT token
    public String generateToken(UserDto userDto) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDto.getUserId());
        claims.put("name", userDto.getName());
        claims.put("email", userDto.getEmail());
        claims.put("fromSchoolId", userDto.getFromSchoolId());
        claims.put("toSchoolId", userDto.getToSchoolId());

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDto.getEmail())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .compact();
    }

    // Method to parse all claims from the token
    public Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    // Method to extract user information from the token
    public UserDto getUserDtoFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        UserDto userDto = new UserDto();
        userDto.setUserId(((Number) claims.get("userId")).longValue());
        userDto.setName((String) claims.get("name"));
        userDto.setEmail((String) claims.get("email"));
        userDto.setFromSchoolId(claims.get("fromSchoolId") != null ? ((Number) claims.get("fromSchoolId")).longValue() : null);
        userDto.setToSchoolId(claims.get("toSchoolId") != null ? ((Number) claims.get("toSchoolId")).longValue() : null);
        return userDto;
    }

    // Method to validate the token
    public boolean validateToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims != null && !isTokenExpired(claims);
    }

    // Method to check if the token is expired
    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    // Method to resolve the token from the HTTP request
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
