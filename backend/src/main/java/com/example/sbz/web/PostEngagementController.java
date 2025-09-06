package com.example.sbz.web;

import com.example.sbz.model.Post;
import com.example.sbz.model.PostReport;
import com.example.sbz.model.User;
import com.example.sbz.repo.PostRepo;
import com.example.sbz.repo.UserRepo;
import com.example.sbz.web.dto.EngagementDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@CrossOrigin(origins = {"http://localhost:5173","http://127.0.0.1:5173"})
@RestController
@RequestMapping("/api/posts")
public class PostEngagementController {

  private final PostRepo postRepo;
  private final UserRepo userRepo;

  public PostEngagementController(PostRepo postRepo, UserRepo userRepo) {
    this.postRepo = postRepo; this.userRepo = userRepo;
  }

  private User me(Authentication auth) {
    if (auth == null || auth.getPrincipal() == null) throw new IllegalArgumentException("No auth");
    String email = Objects.toString(auth.getPrincipal(), null);
    return userRepo.findByEmail(email).orElseThrow();
  }

  /** Toggle like (dodaj/ukloni) i vrati novi broj lajkova. */
  @PostMapping("/{id}/like")
  @Transactional
  public ResponseEntity<EngagementDto> toggleLike(@PathVariable("id") Long id, Authentication auth) {
    User u = me(auth);
    Post p = postRepo.findById(id).orElseThrow();

    if (p.getLikedBy().contains(u)) {
      p.getLikedBy().remove(u);
    } else {
      p.getLikedBy().add(u);
    }
    p = postRepo.save(p);

    int likes = (p.getLikedBy()==null) ? 0 : p.getLikedBy().size();
    return ResponseEntity.ok(new EngagementDto(p.getId(), likes, false));
  }

  /** Jednokratna prijava (idempotentno) — korisnik može prijaviti samo jednom. */
  @PostMapping("/{id}/report")
  @Transactional
  public ResponseEntity<EngagementDto> report(@PathVariable("id") Long id, Authentication auth) {
    User u = me(auth);
    Post p = postRepo.findById(id).orElseThrow();

    boolean already = p.getReports() != null && p.getReports().stream()
        .anyMatch(r -> r.getReporter()!=null && r.getReporter().getId().equals(u.getId()));
    if (!already) {
      PostReport r = new PostReport();
      r.setPost(p);
      r.setReporter(u);
      p.getReports().add(r);     // Post ima @OneToMany(cascade=ALL, orphanRemoval=true)
      p = postRepo.save(p);
    }

    int likes = (p.getLikedBy()==null) ? 0 : p.getLikedBy().size();
    return ResponseEntity.ok(new EngagementDto(p.getId(), likes, true));
  }
}
