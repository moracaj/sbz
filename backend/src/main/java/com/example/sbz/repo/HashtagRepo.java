package com.example.sbz.repo;

import com.example.sbz.model.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HashtagRepo extends JpaRepository<Hashtag, Long> {
  Optional<Hashtag> findByTag(String tag);
  List<Hashtag> findByTagIn(List<String> tags);
}
