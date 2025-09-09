package com.example.sbz.web.dto;

import java.util.List;

public class PlaceDto {
  private Long id;
  private String name;
  private String country;
  private String city;
  private String description;
  private List<String> hashtags;
  private double avgScore;
  private int ratingsCount;

  public PlaceDto() {}

  public PlaceDto(Long id, String name, String country, String city, String description,
                  List<String> hashtags, double avgScore, int ratingsCount) {
    this.id = id; this.name = name; this.country = country; this.city = city;
    this.description = description; this.hashtags = hashtags;
    this.avgScore = avgScore; this.ratingsCount = ratingsCount;
  }

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
  public List<String> getHashtags() { return hashtags; }
  public void setHashtags(List<String> hashtags) { this.hashtags = hashtags; }
  public double getAvgScore() { return avgScore; }
  public void setAvgScore(double avgScore) { this.avgScore = avgScore; }
  public int getRatingsCount() { return ratingsCount; }
  public void setRatingsCount(int ratingsCount) { this.ratingsCount = ratingsCount; }
}
