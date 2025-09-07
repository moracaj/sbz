package com.example.sbz.web;

import com.example.sbz.model.Suspension;
import com.example.sbz.model.SuspensionType;
import com.example.sbz.repo.SuspensionRepo;
import com.example.sbz.service.BadUserDetectionService;
import com.example.sbz.web.dto.MessageDto;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDetectionController {
 
	 private final SuspensionRepo suspensionRepo; // dodaj repo
	private final BadUserDetectionService detectionService;
  //public AdminDetectionController(BadUserDetectionService d) { this.detectionService = d; }
  
	 public AdminDetectionController(SuspensionRepo suspensionRepo,
             BadUserDetectionService detectionService) {
this.suspensionRepo = suspensionRepo;
this.detectionService = detectionService;
}
	
	 @PostMapping("/detect-bad-users")
	  public List<Suspension> detect() {
	    detectionService.detectBadUsers();                  // kreira/produži suspenzije
	    return suspensionRepo.findActive(Instant.now());    // vrati sve aktivne
	  }
  
	 @GetMapping("/suspensions/active")
	  public List<Suspension> active(@RequestParam(required = false) SuspensionType type) {
	    var now = Instant.now();
	    return (type == null) ? suspensionRepo.findActive(now)
	                          : suspensionRepo.findActiveByType(type, now);
	  }

	 @PostMapping("/suspensions/user/{userId}/lift")
	  @Transactional
	  public MessageDto lift(@PathVariable Long userId) {
	    var now = Instant.now();
	    suspensionRepo.deactivateAllForUser(userId, now);
	    return new MessageDto("All active suspensions lifted.");
	  }
}

