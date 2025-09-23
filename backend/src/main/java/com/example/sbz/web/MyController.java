package com.example.sbz.web;

import com.example.sbz.model.Hashtag;
import com.example.sbz.model.Post;
import com.example.sbz.repo.HashtagRepo;
import com.example.sbz.repo.PostRepo;
import com.example.sbz.repo.UserRepo;
import com.example.sbz.web.dto.PostCreateRequest;
import com.example.sbz.web.dto.PostDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import com.example.sbz.model.SuspensionType;
import com.example.sbz.repo.SuspensionRepo;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/me")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")


 public class MyController {
   private final UserRepo userRepo;
   private final PostRepo postRepo;
   private final HashtagRepo hashtagRepo;
   private final SuspensionRepo suspensionRepo;
   public MyController(UserRepo userRepo, PostRepo postRepo, HashtagRepo hashtagRepo, SuspensionRepo suspensionRepo) {
     this.userRepo = userRepo; this.postRepo = postRepo; this.hashtagRepo = hashtagRepo; this.suspensionRepo = suspensionRepo;
   }

  @Transactional(readOnly = true)
  @GetMapping("/posts")
  public ResponseEntity<List<PostDto>> myPosts(Authentication auth) {
    if (auth == null) return ResponseEntity.status(401).build();

    var me = userRepo.findByEmail(auth.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "no user"));

    var posts = postRepo.findByAuthorIdOrderByCreatedAtDesc(me.getId()).stream()
        .map(p -> {
          int likeCount = (p.getLikedBy() == null) ? 0 : p.getLikedBy().size();
          List<String> tags = (p.getHashtags() == null)
              ? List.of()
              : p.getHashtags().stream().map(Hashtag::getTag).toList();
          boolean reported = (p.getReports() != null && !p.getReports().isEmpty());
          return new PostDto(p.getId(), p.getText(), p.getCreatedAt(), tags, likeCount, reported);
        })
        .toList();

    return ResponseEntity.ok(posts);
  }

  @PostMapping("/posts")
  @ResponseStatus(HttpStatus.CREATED)
  public PostDto createPost(@RequestBody PostCreateRequest req, Authentication auth) {
    var me = userRepo.findByEmail(auth.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "no user"));

    var now = java.time.Instant.now();
    var active = suspensionRepo.findByUserAndEndAtAfter(me, now).stream()
        .anyMatch(s -> s.getType() == SuspensionType.POST_BAN);
    if (active) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Zabranjeno objavljivanje – aktivna suspenzija (POST ban).");
    }
    
    
    
    if (req == null || req.getText() == null || req.getText().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "text is required");
    }

    Post p = new Post();
    p.setAuthor(me);
    p.setText(req.getText().trim());
    p.setCreatedAt(Instant.now());

    // prikupi hashtage: iz body-ja ili iz teksta
    Set<String> tagStrings = new HashSet<>();

    if (req.getHashtags() != null && !req.getHashtags().isEmpty()) {
      tagStrings.addAll(
          req.getHashtags().stream()
              .filter(Objects::nonNull)
              .map(s -> s.trim().toLowerCase())
              .map(s -> s.startsWith("#") ? s.substring(1) : s)
              .filter(s -> !s.isBlank())
              .collect(Collectors.toSet())
      );
    } else {
      // iz teksta: #tag
      Pattern PATTERN = Pattern.compile("#([A-Za-z0-9_]{1,30})");
      Matcher m = PATTERN.matcher(req.getText());
      while (m.find()) {
        tagStrings.add(m.group(1).toLowerCase());
      }
    }

    if (!tagStrings.isEmpty()) {
      Set<Hashtag> tagEntities = new HashSet<>();
      for (String t : tagStrings) {
        Hashtag h = hashtagRepo.findByTag(t).orElseGet(() -> {
          Hashtag nh = new Hashtag();
          nh.setTag(t);
          return hashtagRepo.save(nh);
        });
        tagEntities.add(h);
      }
      p.setHashtags(tagEntities);
    }

    p = postRepo.save(p);

    int likeCount = (p.getLikedBy() == null) ? 0 : p.getLikedBy().size();
    List<String> tags = (p.getHashtags() == null)
        ? List.of()
        : p.getHashtags().stream().map(Hashtag::getTag).toList();
    boolean reported = (p.getReports() != null && !p.getReports().isEmpty());

    return new PostDto(p.getId(), p.getText(), p.getCreatedAt(), tags, likeCount, reported);
  }
}
