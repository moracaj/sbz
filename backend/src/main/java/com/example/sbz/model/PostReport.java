package com.example.sbz.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class PostReport {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "reporter_id", nullable = false)
  private User reporter;

  /*@Column(nullable = false)
  private Instant reportedAt = Instant.now();*/
  @Column(nullable = true)
  private Instant createdAt = Instant.now();

  public Instant getCreatedAt() {
      return createdAt;
  }
  public void setCreatedAt(Instant createdAt) {
      this.createdAt = createdAt;
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Post getPost() { return post; }
  public void setPost(Post post) { this.post = post; }
  public User getReporter() { return reporter; }
  public void setReporter(User reporter) { this.reporter = reporter; }
  //public Instant getReportedAt() { return reportedAt; }
  //public void setReportedAt(Instant reportedAt) { this.reportedAt = reportedAt; }
}
