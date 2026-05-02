package com.community.property.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.prefix}")
    private String prefix;

    private static String SECRET;
    private static Long EXPIRATION;
    private static String HEADER;
    private static String PREFIX;

    @PostConstruct
    public void init() {
        SECRET = this.secret;
        EXPIRATION = this.expiration;
        HEADER = this.header;
        PREFIX = this.prefix;
    }

    public static String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        return createToken(claims, username);
    }

    private static String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + EXPIRATION);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }

    public static Claims parseToken(String token) {
        try {
            if (token != null && token.startsWith(PREFIX + " ")) {
                token = token.substring(PREFIX.length() + 1);
            }
            return Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("JWT解析失败：", e);
            return null;
        }
    }

    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("userId", Long.class);
        }
        return null;
    }

    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("username", String.class);
        }
        return null;
    }

    public static String getRole(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("role", String.class);
        }
        return null;
    }

    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims != null) {
                return claims.getExpiration().before(new Date());
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    public static String getHeader() {
        return HEADER;
    }

    public static String getPrefix() {
        return PREFIX;
    }
}