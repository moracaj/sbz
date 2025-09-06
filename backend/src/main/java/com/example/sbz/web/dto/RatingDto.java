package com.example.sbz.web.dto;

import java.time.Instant;
import java.util.List;

public class RatingDto {
  private Long id;
  private Long authorId;
  private String authorName;
  private int score;          // 1..5
  private String comment;
  private List<String> hashtags;
  private Instant createdAt;

  public RatingDto() {}

  public RatingDto(Long id, Long authorId, String authorName, int score, String comment,
                   List<String> hashtags, Instant createdAt) {
    this.id = id; this.authorId = authorId; this.authorName = authorName;
    this.score = score; this.comment = comment; this.hashtags = hashtags; this.createdAt = createdAt;
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getAuthorId() { return authorId; }
  public void setAuthorId(Long authorId) { this.authorId = authorId; }
  public String getAuthorName() { return authorName; }
  public void setAuthorName(String authorName) { this.authorName = authorName; }
  public int getScore() { return score; }
  public void setScore(int score) { this.score = score; }
  public String getComment() { return comment; }
  public void setComment(String comment) { this.comment = comment; }
  public List<String> getHashtags() { return hashtags; }
  public void setHashtags(List<String> hashtags) { this.hashtags = hashtags; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
