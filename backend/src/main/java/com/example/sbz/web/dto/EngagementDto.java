package com.example.sbz.web.dto;

public class EngagementDto {
  private Long postId;
  private int likesCount;
  private boolean reported;

  public EngagementDto() {}
  public EngagementDto(Long postId, int likesCount, boolean reported) {
    this.postId = postId; this.likesCount = likesCount; this.reported = reported;
  }
  public Long getPostId() { return postId; }
  public void setPostId(Long postId) { this.postId = postId; }
  public int getLikesCount() { return likesCount; }
  public void setLikesCount(int likesCount) { this.likesCount = likesCount; }
  public boolean isReported() { return reported; }
  public void setReported(boolean reported) { this.reported = reported; }
}
