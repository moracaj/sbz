package com.example.sbz.web.dto;

import java.time.Instant;
import java.util.List;

public class PostDto {
  private Long id;
  Long authorId;
  String authorName;
  private String text;
  private Instant createdAt;
  private int likesCount;
  private List<String> hashtags;
  private boolean reported;

  public PostDto() {}


  public PostDto(Long id, Long authorId, String authorName, String text, Instant createdAt, int likesCount,
			List<String> hashtags, boolean reported) {
		super();
		this.id = id;
		this.authorId = authorId;
		this.authorName = authorName;
		this.text = text;
		this.createdAt = createdAt;
		this.likesCount = likesCount;
		this.hashtags = hashtags;
		this.reported = reported;
	}
  
  
  
  // radi kompatibilnosti sa starim pozivom (bez heštagova)
  public PostDto(Long id, String text, Instant createdAt, int likesCount) {
    this(id, text, createdAt, likesCount, List.of());
  }

  public PostDto(Long id, String text, Instant createdAt, int likesCount, List<String> hashtags) {
    this.id = id;
    this.text = text;
    this.createdAt = createdAt;
    this.hashtags = hashtags;
    this.likesCount = likesCount;
   
  }
  
  
  public PostDto(Long id, String text, Instant createdAt,
          List<String> hashtags, int likesCount, boolean reported) {
	this.id = id;
	this.text = text;
	this.createdAt = createdAt;
	this.hashtags = hashtags;
	this.likesCount = likesCount;
	this.reported = reported;
}
  

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getText() { return text; }
  public void setText(String text) { this.text = text; }

  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

  public int getLikesCount() { return likesCount; }
  public void setLikesCount(int likesCount) { this.likesCount = likesCount; }

  public List<String> getHashtags() { return hashtags; }
  public void setHashtags(List<String> hashtags) { this.hashtags = hashtags; }

  public boolean isReported() { return reported; }
  public void setReported(boolean reported) { this.reported = reported; }


}
