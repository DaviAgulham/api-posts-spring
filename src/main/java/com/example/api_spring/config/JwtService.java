package com.example.api_spring.config;

import com.example.api_spring.user.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
  private final Key key;
  private final long expirationSeconds;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.expiration-seconds}") long expirationSeconds
  ) {
    this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(encodeToBase64(secret)));
    this.expirationSeconds = expirationSeconds;
  }

  public String generate(User user) {
    Instant now = Instant.now();
    return Jwts.builder()
        .setSubject(user.getEmail())
        .addClaims(Map.of("id", user.getId(), "role", user.getRole().name()))
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plusSeconds(expirationSeconds)))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public Jws<Claims> parse(String token) {
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
  }

  public String getSubject(String token) { return parse(token).getBody().getSubject(); }

  private static String encodeToBase64(String s) {
    return java.util.Base64.getEncoder().encodeToString(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }
}
