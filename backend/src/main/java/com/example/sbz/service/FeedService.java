package com.example.sbz.service;

import com.example.sbz.feed.Candidate;
import com.example.sbz.model.Post;
import com.example.sbz.model.User;
import com.example.sbz.repo.PostRepo;
import com.example.sbz.repo.UserRepo;
import com.example.sbz.web.dto.FeedResponse;
import com.example.sbz.web.dto.PostDto;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeedService {

  private final PostRepo postRepo;
  private final UserRepo userRepo;
  private final KieContainer kieContainer;

  public FeedService(PostRepo postRepo, UserRepo userRepo, KieContainer kieContainer) {
    this.postRepo = postRepo;
    this.userRepo = userRepo;
    this.kieContainer = kieContainer;
  }

  @Transactional(readOnly = true)
  public FeedResponse buildFeed(User viewer) {
    Instant now = Instant.now();

    // 1) prijatelji (24h)
    Set<Long> friendIds = userRepo.findFriendIds(viewer.getId());
    List<Post> friendPosts = friendIds.isEmpty()
        ? List.of()
        : postRepo.recentFromAuthors(friendIds, now.minus(Duration.ofHours(24)));

    // 2) kandidati koji nisu prijatelji (poslednja 3 dana)
    Set<Long> exclude = new HashSet<>(friendIds);
    exclude.add(viewer.getId());
    List<Post> candidates = postRepo.recentNotFromAuthors(exclude, now.minus(Duration.ofDays(3)));

    // 3) popularni heštegovi (24h: >5 objava)
    Map<String, Long> hashtagCounts24h = postRepo.recentAll(now.minus(Duration.ofHours(24))).stream()
        .flatMap(p -> hashtagsOf(p).stream())
        .collect(Collectors.groupingBy(h -> h, Collectors.counting()));
    Set<String> popularHashtags = hashtagCounts24h.entrySet().stream()
        .filter(e -> e.getValue() > 5)
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());

    // 4) tvoji heštegovi (iz sopstvenih objava – poslednjih 7 dana)
    Set<String> ownHashtags = postRepo.recentFromAuthors(List.of(viewer.getId()), now.minus(Duration.ofDays(7))).stream()
        .flatMap(p -> hashtagsOf(p).stream())
        .collect(Collectors.toSet());

    // 5) heštegovi koje lajkuješ (ako nemaš like entitet, ostavi prazno)
    Set<String> likedHashtags = Set.of();

    // 6) Drools scorovanje
    List<Candidate> facts = candidates.stream()
        .map(p -> new Candidate(p.getId(), p.getAuthor().getId(), hashtagsOf(p), likesOf(p), createdOf(p)))
        .toList();

    KieSession ks = kieContainer.newKieSession();
    try {
      ks.setGlobal("now", now);
      ks.setGlobal("popularHashtags", popularHashtags);
      ks.setGlobal("ownHashtags", ownHashtags);
      ks.setGlobal("likedHashtags", likedHashtags);

      facts.forEach(ks::insert);
      ks.fireAllRules();
    } finally {
      ks.dispose();
    }

    // 7) top 20 po score, zatim novije
    List<Long> topIds = facts.stream()
        .sorted(Comparator.comparingInt(Candidate::getScore).reversed()
            .thenComparing(Candidate::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
        .limit(20)
        .map(Candidate::getPostId)
        .toList();

    // mapiranje DTO-a (prvo dohvatimo mapu {id->post})
    Map<Long, Post> postById = candidates.stream().collect(Collectors.toMap(Post::getId, p -> p, (a,b)->a));
    List<PostDto> recommended = topIds.stream().map(id -> toDto(postById.get(id))).toList();
    List<PostDto> friends = friendPosts.stream().map(this::toDto).toList();

    return new FeedResponse(friends, recommended);
  }

  // --- helpers za mapiranje/parsiranje ---
  private PostDto toDto(Post p) {
    if (p == null) return null;
    User a = p.getAuthor();
    String authorName = (a == null) ? "Unknown" : (safe(a.getFirstName()) + " " + safe(a.getLastName())).trim();
    return new PostDto(
        p.getId(),
        a != null ? a.getId() : null,
        authorName,
        textOf(p),
        createdOf(p),
        likesOf(p),
        p.getHashtags(),
        false
    );
  }

  private static String textOf(Post p) {
    // prilagodi ovom metodom ako je polje npr. getContent()
    try { return (String) Post.class.getMethod("getText").invoke(p); }
    catch (Exception ignored) {}
    try { return (String) Post.class.getMethod("getContent").invoke(p); }
    catch (Exception ignored) {}
    return "";
  }

  private static Instant createdOf(Post p) {
    try { return (Instant) Post.class.getMethod("getCreatedAt").invoke(p); }
    catch (Exception ignored) {}
    try { return (Instant) Post.class.getMethod("getCreated").invoke(p); }
    catch (Exception ignored) {}
    return Instant.now();
  }

  @SuppressWarnings("unchecked")
  private static Set<String> hashtagsOf(Post p) {
    // očekujemo Set<String> getHashtags(); fallback: String getHashtagsCsv()
    try { return (Set<String>) Post.class.getMethod("getHashtags").invoke(p); }
    catch (Exception ignored) {}
    try {
      String csv = (String) Post.class.getMethod("getHashtagsCsv").invoke(p);
      if (csv == null || csv.isBlank()) return Set.of();
      return Arrays.stream(csv.split("[,#\\s]+"))
          .filter(s -> !s.isBlank()).map(s -> s.toLowerCase()).collect(Collectors.toSet());
    } catch (Exception ignored) {}
    return Set.of();
  }

  private static int likesOf(Post p) {
    try { return (int) Post.class.getMethod("getLikes").invoke(p); }
    catch (Exception ignored) {}
    try { Integer x = (Integer) Post.class.getMethod("getLikeCount").invoke(p); return x==null?0:x; }
    catch (Exception ignored) {}
    return 0;
  }

  private static String safe(String s){ return s==null? "": s; }
}
