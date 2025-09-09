package com.example.sbz.web.dto;

import java.util.List;

public class RatingCreateRequest {
  public int score;              // 1..5
  public String comment;
  public List<String> hashtags;  // opciono
}
