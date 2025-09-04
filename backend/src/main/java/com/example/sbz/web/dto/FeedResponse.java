package com.example.sbz.web.dto;

import java.util.List;

public record FeedResponse(
  List<PostDto> friends,
  List<PostDto> recommended
) {}
