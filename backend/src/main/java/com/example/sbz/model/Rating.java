package com.example.sbz.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Set;

@Entity
@Table(name="ratings")
public class Rating {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional=false)
  private Place place;

  @ManyToOne(optional=false)
  private User author;

  @Column(nullable=false)
  private int score; // 1-5

  @Column(columnDefinition="TEXT")
  private String comment;

  @ManyToMany
  @JoinTable(name="rating_hashtags",
     joinColumns=@JoinColumn(name="rating_id"),
     inverseJoinColumns=@JoinColumn(name="hashtag_id"))
  private Set<Hashtag> hashtags;

  private Instant createdAt = Instant.now();

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Place getPlace() { return place; }
  public void setPlace(Place place) { this.place = place; }
  public User getAuthor() { return author; }
  public void setAuthor(User author) { this.author = author; }
  public int getScore() { return score; }
  public void setScore(int score) { this.score = score; }
  public String getComment() { return comment; }
  public void setComment(String comment) { this.comment = comment; }
  public Set<Hashtag> getHashtags() { return hashtags; }
  public void setHashtags(Set<Hashtag> hashtags) { this.hashtags = hashtags; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
