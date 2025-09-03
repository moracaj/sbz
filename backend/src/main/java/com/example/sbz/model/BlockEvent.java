package com.example.sbz.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class BlockEvent {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional=false)
  private User blockedUser;

  @ManyToOne(optional=false)
  private User byUser;

  private Instant occurredAt = Instant.now();

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public User getBlockedUser() { return blockedUser; }
  public void setBlockedUser(User blockedUser) { this.blockedUser = blockedUser; }
  public User getByUser() { return byUser; }
  public void setByUser(User byUser) { this.byUser = byUser; }
  public Instant getOccurredAt() { return occurredAt; }
  public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}
