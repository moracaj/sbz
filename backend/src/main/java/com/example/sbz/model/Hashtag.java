package com.example.sbz.model;

import jakarta.persistence.*;

@Entity
@Table(name="hashtags", uniqueConstraints=@UniqueConstraint(columnNames="tag"))
public class Hashtag {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  private Long id;

  @Column(nullable=false)
  private String tag;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getTag() { return tag; }
  public void setTag(String tag) { this.tag = tag; }
}
