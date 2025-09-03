package com.example.sbz.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "posts")
public class Post {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional=false)
  private User author;

  @Column(columnDefinition="TEXT")
  private String text;

  private Instant createdAt = Instant.now();

  @ManyToMany
  @JoinTable(name="post_hashtags",
     joinColumns=@JoinColumn(name="post_id"),
     inverseJoinColumns=@JoinColumn(name="hashtag_id"))
  private Set<Hashtag> hashtags;

  @ManyToMany
  @JoinTable(name="post_likes",
     joinColumns=@JoinColumn(name="post_id"),
     inverseJoinColumns=@JoinColumn(name="user_id"))
  private Set<User> likedBy;

  @OneToMany(mappedBy="post", cascade=CascadeType.ALL, orphanRemoval=true)
  private Set<PostReport> reports;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public User getAuthor() { return author; }
  public void setAuthor(User me) { this.author = me; }
  public String getText() { return text; }
  public void setText(String text) { this.text = text; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
  public Set<Hashtag> getHashtags() { return hashtags; }
  public void setHashtags(Set<Hashtag> hashtags) { this.hashtags = hashtags; }
  public Set<User> getLikedBy() { return likedBy; }
  public void setLikedBy(Set<User> likedBy) { this.likedBy = likedBy; }
  public Set<PostReport> getReports() { return reports; }
  public void setReports(Set<PostReport> reports) { this.reports = reports; }
}
