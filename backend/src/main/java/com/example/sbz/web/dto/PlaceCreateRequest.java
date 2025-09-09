package com.example.sbz.web.dto;

import java.util.List;

public class PlaceCreateRequest {
  public String name;
  public String country;
  public String city;
  public String description;
  public List<String> hashtags; // npr. ["food","ns","museum"]
}
