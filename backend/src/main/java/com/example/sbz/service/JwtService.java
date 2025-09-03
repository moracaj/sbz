package com.example.sbz.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

  @Value("${app.jwt.secret}")
  private String secret;

  @Value("${app.jwt.expires-in-seconds}")
  private long expiresInSeconds;

  /** pravi token: subject=email, claimovi: uid, admin */
  public String generate(Long userId, String email, boolean admin) {
    var now = Instant.now();
    var key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    return Jwts.builder()
        .subject(email)
        .claim("uid", userId)
        .claim("admin", admin)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(expiresInSeconds)))
        .signWith(key, Jwts.SIG.HS256)
        .compact();
  }

  /** validira i vraća Claims; baca JwtException ako ne važi */
  public Claims parse(String token) {
    var key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
