package com.example.sbz.model;

import jakarta.persistence.*;
import java.time.Instant;
@Entity
public class BlockEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // koga blokira
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private User target;

    // ko je blokirao (ako ti treba)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "by_user_id", nullable = false)
    private User byUser;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public User getTarget() { return target; }
    public void setTarget(User target) { this.target = target; }
    public User getByUser() { return byUser; }
    public void setByUser(User byUser) { this.byUser = byUser; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
