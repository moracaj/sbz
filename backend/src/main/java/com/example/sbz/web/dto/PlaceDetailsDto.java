package com.example.sbz.web.dto;

import java.util.List;

public class PlaceDetailsDto extends PlaceDto {
  private List<RatingDto> ratings;

  public PlaceDetailsDto() {}

  public PlaceDetailsDto(PlaceDto base, List<RatingDto> ratings) {
    super(base.getId(), base.getName(), base.getCountry(), base.getCity(),
          base.getDescription(), base.getHashtags(), base.getAvgScore(), base.getRatingsCount());
    this.ratings = ratings;
  }

  public List<RatingDto> getRatings() { return ratings; }
  public void setRatings(List<RatingDto> ratings) { this.ratings = ratings; }
}
