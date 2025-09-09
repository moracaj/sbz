package com.example.sbz.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Suspension {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional=false)
  private User user;

  private Instant startAt;
  private Instant endAt;

  @Enumerated(EnumType.STRING)
  private SuspensionType type;
  
  @Transient
  public boolean isActiveAt(Instant ref) {
      return endAt == null || endAt.isAfter(ref);
  }
  
  @Transient
  public boolean isActive() {
      return isActiveAt(Instant.now());
  }
  
  @Column(columnDefinition = "TEXT")
  private String reason;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }
  public Instant getStartAt() { return startAt; }
  public void setStartAt(Instant startAt) { this.startAt = startAt; }
  public Instant getEndAt() { return endAt; }
  public void setEndAt(Instant endAt) { this.endAt = endAt; }
  public SuspensionType getType() { return type; }
  public void setType(SuspensionType type) { this.type = type; }
  public String getReason() { return reason; }
  public void setReason(String reason) { this.reason = reason; }
}
