package com.example.sbz.web;

import com.example.sbz.model.Suspension;
import com.example.sbz.service.BadUserDetectionService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDetectionController {
  private final BadUserDetectionService detectionService;
  public AdminDetectionController(BadUserDetectionService d) { this.detectionService = d; }
  
  @PostMapping("/detect-bad-users")
  public List<Suspension> detect() {
    return detectionService.detectBadUsers();
  }
}
