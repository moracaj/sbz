package com.example.sbz.service;

import com.example.sbz.model.Suspension;
import com.example.sbz.model.User;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class BadUserDetectionService {
	
  private final KieContainer kieContainer;
  private final PostReportRepo postReportRepo;
  private final BlockEventRepo blockEventRepo;
  private final UserRepo userRepo;
  private final SuspensionRepo suspensionRepo;

  public BadUserDetectionService(KieContainer kc, PostReportRepo r, BlockEventRepo b, UserRepo u, SuspensionRepo s) {
    this.kieContainer = kc; this.postReportRepo = r; this.blockEventRepo = b; this.userRepo = u; this.suspensionRepo = s;
  }

  /**
   * Pokreće pravila, skuplja predloge suspenzija i upisuje/produžava ih u bazi.
   * Vraća listu suspenzija koje su kreirane ili ažurirane ovim pozivom.
   */
  @Transactional
  public List<Suspension> detectBadUsers() {
    Instant now = Instant.now();
    Instant since = now.minus(Duration.ofDays(2)); // vremenski prozor za evente (možeš menjati)

    KieSession ks = kieContainer.newKieSession();
    try {
      // globali za pravila
      ks.setGlobal("now", now);
      List<Suspension> sink = new ArrayList<>();
      ks.setGlobal("suspensionSink", sink);

      // ubacujemo sve korisnike kao činjenice
      userRepo.findAll().forEach(ks::insert);
     

      ks.setGlobal("NOW", now);
      ks.setGlobal("SINCE_1D", now.minus(1, ChronoUnit.DAYS));
      ks.setGlobal("SINCE_2D", now.minus(2, ChronoUnit.DAYS));
      ks.setGlobal("SINCE_6H", now.minus(6, ChronoUnit.HOURS));

     
      ks.setGlobal("sink", sink);
      // ubacujemo evente iz poslednja 2 dana (blokovi + prijave)
    /*  blockEventRepo.findAllAfter(since).forEach(ks::insert);
      postReportRepo.findAllAfter(since).forEach(ks::insert);*/
      blockEventRepo.findAllAfter(now.minus(2, ChronoUnit.DAYS)).forEach(ks::insert);
      postReportRepo.findAllAfter(now.minus(2, ChronoUnit.DAYS)).forEach(ks::insert);

      ks.fireAllRules();

      // deduplikacija i persist
      List<Suspension> saved = new ArrayList<>();
      for (Suspension s : sink) {
        // osveži user entitet iz persistence konteksta (pravila rade nad detached instancom)
        Long uid = s.getUser().getId();
        User managedUser = userRepo.findById(uid).orElse(null);
        if (managedUser == null) continue;

        s.setUser(managedUser);
        //s.setStartAt(now);

        // Ako već postoji aktivna suspenzija istog tipa, samo je eventualno produži
        List<Suspension> actives = suspensionRepo.findByUserAndEndAtAfter(managedUser, now);
        Suspension sameTypeActive = actives.stream()
          //.filter(a -> a.isActive() && a.getType() == s.getType())
        		.filter(a -> a.getType() == s.getType()) 
        		.filter(a -> a.getEndAt() != null && a.getEndAt().isAfter(now))
          .findFirst().orElse(null);

        if (sameTypeActive != null) {
        	String prev = sameTypeActive.getReason() == null ? "" : sameTypeActive.getReason();
        	  String add  = s.getReason() == null ? "" : s.getReason();
        	  if (!prev.contains(add)) {
        	    String joined = (prev.isBlank() ? add : prev + " | " + add);
        	    if (joined.length() > 1024) joined = joined.substring(0, 1024); // čuvaj limit
        	    sameTypeActive.setReason(joined);
        	  }
          boolean extended = false;
          if (s.getEndAt() != null && sameTypeActive.getEndAt() != null &&
              s.getEndAt().isAfter(sameTypeActive.getEndAt())) {
            sameTypeActive.setEndAt(s.getEndAt());
            extended = true;
          }
          // Zabeleži i razlog nove detekcije (korisno za audit)
          String reason = sameTypeActive.getReason() == null ? "" : sameTypeActive.getReason() + " | ";
          sameTypeActive.setReason(reason + s.getReason());
          if (extended) {
            saved.add(suspensionRepo.save(sameTypeActive));
          } else {
            // ništa novo za snimanje, ali radi konzistentnosti dodaj u rezultat
            saved.add(sameTypeActive);
          }
        } else {
          saved.add(suspensionRepo.save(s));
        }
      }
      return saved;
    } finally {
      ks.dispose();
    }
  }
}
