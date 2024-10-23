package com.example.social_media.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.social_media.dto.user.UserDto;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long expiration;

    public String generateToken(UserDto userDto) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDto.getUserId());
        claims.put("email", userDto.getEmail());

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDto.getEmail())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .compact();
    }

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

    public UserDto getUserDtoFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        UserDto userDto = new UserDto();
        userDto.setUserId(((Number) claims.get("userId")).longValue());
        userDto.setEmail((String) claims.get("email"));
        return userDto;
    }

    public boolean validateToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims != null && !isTokenExpired(claims);
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
