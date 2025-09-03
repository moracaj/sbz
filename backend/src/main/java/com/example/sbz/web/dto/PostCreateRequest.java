package com.example.sbz.web.dto;

import java.util.List;

public class PostCreateRequest {
  private String text;
  private List<String> hashtags; // opcionalno, možeš i prazno

  public String getText() { return text; }
  public void setText(String text) { this.text = text; }

  public List<String> getHashtags() { return hashtags; }
  public void setHashtags(List<String> hashtags) { this.hashtags = hashtags; }
}
