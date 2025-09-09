package com.example.sbz.web;

import com.example.sbz.model.Suspension;
import com.example.sbz.model.SuspensionType;
import com.example.sbz.model.User;
import com.example.sbz.repo.SuspensionRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173","http://127.0.0.1:5173"})
@RestController
@RequestMapping("/api/suspensions") // VAN /api/admin
@RequiredArgsConstructor
public class SuspensionPublicController {

  private final SuspensionRepo suspensionRepo;
  public SuspensionPublicController(SuspensionRepo suspensionRepo) {
	  this.suspensionRepo=suspensionRepo;
  }

  // Minimalni DTO da izbegnemo LAZY probleme pri JSON serijalizaciji
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
    User u = s.getUser();
    String name = (u == null) ? "Unknown"
        : ((u.getFirstName()==null?"":u.getFirstName()) + " " + (u.getLastName()==null?"":u.getLastName())).trim();
    return new SuspensionDto(
        s.getId(),
        s.getType() != null ? s.getType().name() : null,
        (u != null) ? u.getId() : null,
        name,
        s.getStartAt(),
        s.getEndAt(),
        s.getReason()
    );
  }

  // GET /api/suspensions/active  (vidljivo SVIM prijavljenim)
  @GetMapping("/active")
  @Transactional
  public List<SuspensionDto> active(@RequestParam(name = "type", required = false) SuspensionType type) {
    var now = Instant.now();
    var list = (type == null) ? suspensionRepo.findActive(now)
                              : suspensionRepo.findActiveByType(type, now);
    return list.stream().map(SuspensionPublicController::toDto).toList();
  }
}
