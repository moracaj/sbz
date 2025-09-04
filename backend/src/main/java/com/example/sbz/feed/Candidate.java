package com.example.sbz.feed;

import java.time.Instant;
import java.time.Duration;
import java.util.*;

public class Candidate {
  private Long postId;
  private Long authorId;
  private Set<String> hashtags = new HashSet<>();
  private int likes;
  private Instant createdAt;

  private int score = 0;
  private final List<String> reasons = new ArrayList<>();

  public Candidate(Long postId, Long authorId, Collection<String> hashtags, int likes, Instant createdAt) {
    this.postId = postId;
    this.authorId = authorId;
    if (hashtags != null) {
      for (String h : hashtags) if (h != null) this.hashtags.add(h.toLowerCase());
    }
    this.likes = likes;
    this.createdAt = createdAt;
  }

  // --- helpers koje koristi DRL ---
  public boolean isYoungerThanHours(long h, Instant now) {
    return createdAt != null && now != null && createdAt.isAfter(now.minus(Duration.ofHours(h)));
  }
  public boolean hasAny(Set<?> tags) {
    if (tags == null || tags.isEmpty() || hashtags.isEmpty()) return false;
    for (Object o : tags) {
      if (o == null) continue;
      String t = o.toString().toLowerCase();
      if (hashtags.contains(t)) return true;
    }
    return false;
  }
  public void addScore(int delta, String reason) { this.score += delta; if (reason != null) reasons.add(reason); }

  // --- getters ---
  public Long getPostId() { return postId; }
  public Long getAuthorId() { return authorId; }
  public Set<String> getHashtags() { return hashtags; }
  public int getLikes() { return likes; }
  public Instant getCreatedAt() { return createdAt; }
  public int getScore() { return score; }
  public List<String> getReasons() { return reasons; }
}
