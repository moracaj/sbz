package com.example.sbz.service;

import com.example.sbz.model.Suspension;
import com.example.sbz.repo.BlockEventRepo;
import com.example.sbz.repo.PostReportRepo;
import com.example.sbz.repo.SuspensionRepo;
import com.example.sbz.repo.UserRepo;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class BadUserDetectionService {
  private final KieContainer kieContainer;
  private final PostReportRepo reportRepo;
  private final BlockEventRepo blockRepo;
  private final UserRepo userRepo;
  private final SuspensionRepo suspensionRepo;

  public BadUserDetectionService(KieContainer kc, PostReportRepo r, BlockEventRepo b, UserRepo u, SuspensionRepo s) {
    this.kieContainer = kc; this.reportRepo = r; this.blockRepo = b; this.userRepo = u; this.suspensionRepo = s;
  }

  @Transactional
  public List<Suspension> run() {
    KieSession ks = kieContainer.newKieSession();
    try {
      Instant now = Instant.now();
      Instant twoDaysAgo = now.minus(Duration.ofHours(48));

      userRepo.findAll().forEach(ks::insert);
      reportRepo.findAllAfter(twoDaysAgo).forEach(ks::insert);
      blockRepo.findAllAfter(twoDaysAgo).forEach(ks::insert);

      List<Suspension> out = new ArrayList<>();
      ks.setGlobal("suspensionSink", out);
      ks.fireAllRules();

      out.forEach(suspensionRepo::save);
      return out;
    } finally {
      ks.dispose();
    }
  }
}
