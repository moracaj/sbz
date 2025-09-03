package com.example.sbz.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class PostReport {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional=false)
  private Post post;

  @ManyToOne(optional=false)
  private User reporter;

  private Instant reportedAt = Instant.now();

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Post getPost() { return post; }
  public void setPost(Post post) { this.post = post; }
  public User getReporter() { return reporter; }
  public void setReporter(User reporter) { this.reporter = reporter; }
  public Instant getReportedAt() { return reportedAt; }
  public void setReportedAt(Instant reportedAt) { this.reportedAt = reportedAt; }
}
