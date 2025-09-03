package com.example.sbz.model;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name="places")
public class Place {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String country;
  private String city;

  @Column(columnDefinition="TEXT")
  private String description;

  @ManyToMany
  @JoinTable(name="place_hashtags",
     joinColumns=@JoinColumn(name="place_id"),
     inverseJoinColumns=@JoinColumn(name="hashtag_id"))
  private Set<Hashtag> hashtags;

  @OneToMany(mappedBy="place", cascade=CascadeType.ALL, orphanRemoval=true)
  private Set<Rating> ratings;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getCountry() { return country; }
  public void setCountry(String country) { this.country = country; }
  public String getCity() { return city; }
  public void setCity(String city) { this.city = city; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public Set<Hashtag> getHashtags() { return hashtags; }
  public void setHashtags(Set<Hashtag> hashtags) { this.hashtags = hashtags; }
  public Set<Rating> getRatings() { return ratings; }
  public void setRatings(Set<Rating> ratings) { this.ratings = ratings; }
}
