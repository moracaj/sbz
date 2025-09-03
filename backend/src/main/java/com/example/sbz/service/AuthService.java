package com.example.sbz.service;

import com.example.sbz.model.*;
import com.example.sbz.repo.*;
import com.example.sbz.web.dto.*;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuthService {

  private final UserRepo userRepo;
  private final PlaceRepo placeRepo;
  private final SuspensionRepo suspensionRepo;
  private final PasswordEncoder encoder;
  private final JwtService jwt;
  private final KieContainer kieContainer;

  public AuthService(UserRepo userRepo, PlaceRepo placeRepo, SuspensionRepo suspensionRepo,
                     PasswordEncoder encoder, JwtService jwt, KieContainer kc) {
    this.userRepo = userRepo; this.placeRepo=placeRepo; this.suspensionRepo=suspensionRepo;
    this.encoder=encoder; this.jwt=jwt; this.kieContainer=kc;
  }

  @Transactional
  public AuthResponse register(RegisterRequest req) {
    // KIE session za validaciju registracije
    KieSession ks = kieContainer.newKieSession();
    try {
      List<String> errors = new ArrayList<>();
      ks.setGlobal("errors", errors);
      // činjenice: postojeći korisnici, mesta i zahtev
      userRepo.findAll().forEach(ks::insert);
      placeRepo.findAll().forEach(ks::insert);
      ks.insert(req);
      ks.fireAllRules();

      if (!errors.isEmpty()) {
        throw new IllegalArgumentException(String.join("; ", errors));
      }

      User u = new User();
      u.setFirstName(req.getFirstName());
      u.setLastName(req.getLastName());
      u.setEmail(req.getEmail());
      u.setPasswordHash(encoder.encode(req.getPassword()));
      u.setAdmin(false);
      u.setCreatedAt(Instant.now());
      if (req.getHomePlaceId()!=null) {
        Place p = placeRepo.findById(req.getHomePlaceId()).orElse(null);
        u.setHomePlace(p);
      }
      u = userRepo.save(u);
      String token = jwt.generate(u.getId(), u.getEmail(), u.isAdmin());
      return new AuthResponse(token, u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), u.isAdmin());
    } finally { ks.dispose(); }
  }

  @Transactional(readOnly = true)
  public AuthResponse login(LoginRequest req) {
    KieSession ks = kieContainer.newKieSession();
    try {
      List<String> errors = new ArrayList<>();
      ks.setGlobal("errors", errors);
      ks.setGlobal("passwordEncoder", encoder);

      // činjenice: svi korisnici, aktivne suspenzije i zahtev
      userRepo.findAll().forEach(ks::insert);
      // aktivne suspenzije
      Instant now = Instant.now();
      suspensionRepo.findAll().forEach(ks::insert); // pravilo proverava endAt > now
      ks.insert(req);
      ks.fireAllRules();

      if (!errors.isEmpty()) {
        throw new IllegalArgumentException(String.join("; ", errors));
      }

      // dohvat korisnika i token
      User u = userRepo.findByEmail(req.getEmail()).orElseThrow();
      String token = jwt.generate(u.getId(), u.getEmail(), u.isAdmin());
      return new AuthResponse(token, u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), u.isAdmin());
    } finally { ks.dispose(); }
  }
}
