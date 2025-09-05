package com.example.sbz.web;

import com.example.sbz.model.User;
import com.example.sbz.repo.UserRepo;
import com.example.sbz.service.FeedService;
import com.example.sbz.web.dto.FeedResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

  private final FeedService feedService;
  private final UserRepo userRepo;

  public FeedController(FeedService feedService, UserRepo userRepo) {
    this.feedService = feedService; this.userRepo = userRepo;
  }

  private User me(Authentication auth){
    Object p = auth.getPrincipal();
    String username = (p instanceof org.springframework.security.core.userdetails.UserDetails ud)
        ? ud.getUsername() : String.valueOf(p);
    return userRepo.findByEmail(username).orElseThrow();
  }

  @GetMapping
  public FeedResponse getFeed(Authentication auth){
    return feedService.buildFeed(me(auth));
  }
}
