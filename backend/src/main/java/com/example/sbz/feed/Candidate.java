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
  private List<Integer> scoreEvents = new ArrayList<>();

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
  public void addScore(int delta, String reason) { 
	  this.score += delta; 
	  if (reason != null) 
		  reasons.add(reason); 
  }
  
  
  
  
  // === Helper 1: da li je objavu lajkovao bar jedan korisnik iz zadatog skupa ===
  @SuppressWarnings("unchecked")
  public boolean likedByAny(Set<Long> userIds, Map<Long, Set<Long>> likersByPost) {
      if (userIds == null || userIds.isEmpty() || likersByPost == null) return false;
      Set<Long> likers = likersByPost.get(postId);
      if (likers == null || likers.isEmpty()) return false;
      for (Long u : likers) if (userIds.contains(u)) return true;
      return false;
  }

  // === Helper 2: ≥threshold preklapanje lajkera sa bilo kojom objavom koju je gledalac lajkovao ===
  @SuppressWarnings("unchecked")
  public boolean likerOverlapAtLeast(Set<Long> viewerLikedPostIds, Map<Long, Set<Long>> likersByPost, double thr) {
      if (viewerLikedPostIds == null || viewerLikedPostIds.isEmpty() || likersByPost == null) return false;
      Set<Long> a = likersByPost.get(postId);
      if (a == null || a.isEmpty()) return false;

      for (Long otherPostId : viewerLikedPostIds) {
          Set<Long> b = likersByPost.get(otherPostId);
          if (b == null || b.isEmpty()) continue;
          int inter = 0;
          for (Long u : a) if (b.contains(u)) inter++;
          // “70% korisnika koji su lajkovali jednu pojave se i u drugoj”
          double base = Math.max(1, a.size()); // da izbegnemo /0
          if (inter / base >= thr) return true;
      }
      return false;
  }
  

  // --- getters ---
  public Long getPostId() { return postId; }
  public Long getAuthorId() { return authorId; }
  public Set<String> getHashtags() { return hashtags; }
  public int getLikes() { return likes; }
  public Instant getCreatedAt() { return createdAt; }
  public int getScore() { return score; }
  public List<String> getReasons() { return reasons; }
}
