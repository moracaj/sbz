package com.example.sbz.config;

import com.example.sbz.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  public JwtAuthFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest req) {
    String p = req.getServletPath();
    return p.startsWith("/api/auth/") || p.startsWith("/h2");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {

    String auth = req.getHeader("Authorization");
    if (auth != null && auth.startsWith("Bearer ")) {
      try {
        String token = auth.substring(7);
        Claims claims = jwtService.parse(token);

        String email = claims.getSubject();
        Boolean isAdmin = claims.get("admin", Boolean.class);

        var authorities = isAdmin
        	    ? List.of(new SimpleGrantedAuthority("ADMIN"))
        	    : List.of(new SimpleGrantedAuthority("USER"));

        var authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (JwtException | IllegalArgumentException ex) {
        SecurityContextHolder.clearContext(); // ne važi token -> neće biti autentifikovan
      }
    }

    chain.doFilter(req, res);
  }
}
