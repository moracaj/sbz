

package com.example.sbz.web;

import com.example.sbz.model.Suspension;
import com.example.sbz.model.User;
import com.example.sbz.repo.SuspensionRepo;
import com.example.sbz.repo.UserRepo;
import com.example.sbz.service.BadUserDetectionService;
import com.example.sbz.web.dto.MessageDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@CrossOrigin(origins = {"http://localhost:5173","http://127.0.0.1:5173"})
@RestController
@RequestMapping("/api/detection") // >>> VAN /api/admin
@RequiredArgsConstructor
public class AdminDetectionController {

  private final SuspensionRepo suspensionRepo;
  private final BadUserDetectionService detectionService;
  private final UserRepo userRepo;
  
  public AdminDetectionController(SuspensionRepo suspensionRepo,
          BadUserDetectionService detectionService, UserRepo userRepo) {
			this.suspensionRepo = suspensionRepo;
			this.detectionService = detectionService;
			this.userRepo=userRepo;}

  // --- places-style helpers ---
  private User me(Authentication auth){
    if (auth == null || auth.getPrincipal() == null)
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No auth");
    String email = Objects.toString(auth.getPrincipal(), null);
    return userRepo.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
  }

  private void requireAdmin(User u){
    if (u == null || !u.isAdmin())
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
  }

  // Minimalni DTO da izbegnemo LAZY eksploziju
  public static record SuspensionDto(
      Long id,
      String type,
      Long userId,
      String userName,
      Instant startAt,
      Instant endAt,
      String reason
  ) {}

  private static SuspensionDto toDto(Suspension s){
    var u = s.getUser();
    String name = (u == null) ? "Unknown"
        : ((u.getFirstName()==null?"":u.getFirstName()) + " " + (u.getLastName()==null?"":u.getLastName())).trim();
    return new SuspensionDto(
        s.getId(),
        s.getType() != null ? s.getType().name() : null,
        u != null ? u.getId() : null,
        name,
        s.getStartAt(),
        s.getEndAt(),
        s.getReason()
    );
  }

  // ADMIN ONLY: pokretanje detekcije -> vrati aktivne kao DTO
  @PostMapping("/run")
  @Transactional
  public List<SuspensionDto> run(Authentication auth) {
    User admin = me(auth);
    requireAdmin(admin);

    detectionService.detectBadUsers(); // kreira/produži suspenzije

    var now = Instant.now();
    var list = suspensionRepo.findActive(now); // učitamo entitete u T/X...
    return list.stream().map(AdminDetectionController::toDto).toList(); // ...pretvorimo u DTO dok je sesija otvorena
  }

  // ADMIN ONLY: skidanje suspenzija
  @PostMapping("/user/{userId}/lift")
  @Transactional
  public MessageDto lift(@PathVariable Long userId, Authentication auth) {
    User admin = me(auth);
    requireAdmin(admin);

    var now = Instant.now();
    suspensionRepo.deactivateAllForUser(userId, now);
    return new MessageDto("All active suspensions lifted.");
  }
}

