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
        .flatMap(p ->  hashtagsListOf(p).stream())
        .collect(Collectors.groupingBy(h -> h, Collectors.counting()));
    Set<String> popularHashtags = hashtagCounts24h.entrySet().stream()
        .filter(e -> e.getValue() > 5)
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());

    // 4) tvoji heštegovi (iz sopstvenih objava – poslednjih 7 dana)
    Set<String> ownHashtags = postRepo.recentFromAuthors(List.of(viewer.getId()), now.minus(Duration.ofDays(7))).stream()
        .flatMap(p -> hashtagsListOf(p).stream())
        .collect(Collectors.toSet());

    // 5) heštegovi koje lajkuješ (ako nemaš like entitet, ostavi prazno)
    Set<String> likedHashtags = Set.of();

    // 6) Drools scorovanje
    List<Candidate> facts = candidates.stream()
        .map(p -> new Candidate(p.getId(), p.getAuthor().getId(),  hashtagsListOf(p), likesOf(p), createdOf(p)))
        .toList();

    KieSession ks = kieContainer.newKieSession();
    try {
      ks.setGlobal("now", now);
      ks.setGlobal("popularHashtags", popularHashtags);
      ks.setGlobal("ownHashtags", ownHashtags);
      ks.setGlobal("likedHashtags", likedHashtags);

      facts.forEach(ks::insert);
      ks.fireAllRules();
      
      
      /*////////////////////////////////////////*/
      System.out.println("viewerId = " + viewer.getId());
   // --- ADVANCED SCORING (ako korisnik NIJE nov) ---
      boolean isNewUser =
          friendIds.isEmpty() &&
          postRepo.countByAuthor(viewer.getId()) == 0;   // napravi ovu metodu u PostRepo, ili zameni proverenim načinom

      if (!isNewUser) {
        // 1) priprema podataka o lajkovima
        // U PostRepo dodaj metodu koja za skup postId vrati mapu {postId -> skup userId koji su lajkovali}
        Set<Long> candidateIds = facts.stream().map(Candidate::getPostId).collect(Collectors.toSet());

        // sve postove koje gledamo za korelaciju (kandidati + postovi koje je korisnik lajkovao)
        // U PostRepo dodaj metodu: findPostsUserLiked(userId, since) koja vraća listu postova koje je user lajkovao
        Instant threeDaysAgo = now.minus(Duration.ofDays(3));
        List<Post> viewerLikedPosts = postRepo.findPostsUserLiked(viewer.getId(), null); // null = bez roka, ili stavi threeDaysAgo ako želiš
        Set<Long> viewerLikedIds = viewerLikedPosts.stream().map(Post::getId).collect(Collectors.toSet());

        Set<Long> allForLikers = new HashSet<>(candidateIds);
        allForLikers.addAll(viewerLikedIds);

       // Map<Long, Set<Long>> likersByPost = postRepo.findLikersByPostIds(allForLikers); // {postId -> set(userId)}

        List<Object[]> pairs = postRepo.findLikerPairsByPostIds(allForLikers);
     // u Map<Long, Set<Long>>
     Map<Long, Set<Long>> likersByPost = new HashMap<>();
     for (Object[] row : pairs) {
       Long postId = (Long) row[0];
       Long userId = (Long) row[1];
       likersByPost.computeIfAbsent(postId, k -> new HashSet<>()).add(userId);
     }
        
        
        // skup korisnika sličnih posmatraču (Pearson >= 0.5)
        Set<Long> similarUsers = computeSimilarUsers(viewer.getId(), likersByPost, 0.1);

        System.out.println("viewerLikedIds = " + viewerLikedIds);
        System.out.println("similarUsers   = " + similarUsers);
        Map<Long, Candidate> byId = facts.stream()
        	    .collect(Collectors.toMap(Candidate::getPostId, c -> c));
        for (Long pid : candidateIds) {
            Candidate c = byId.get(pid);
            if (c == null) continue;

            Set<Long> likers = likersByPost.getOrDefault(pid, Set.of());

            boolean likedBySimilar = !Collections.disjoint(likers, similarUsers);
            boolean similarToLiked = isSimilarToAnyUserLiked(pid, viewerLikedIds, likersByPost, 0.70);

           // if (likedBySimilar) c.addScore(1, "similarUsersLiked");
          //  if (similarToLiked) c.addScore(1, "similarPost");

            // debug ispis
            System.out.println("CAND " + pid +
                " likers=" + likers +
                " likedBySimilar=" + likedBySimilar +
                " similarToLiked=" + similarToLiked);
        }

        
        
        
        // preferencije: heštegovi koje je korisnik lajkovao >= 3 puta u poslednja 3 dana
        // Dodaj u PostRepo: findPostsUserLiked(userId, threeDaysAgo)
        Map<String, Long> liked3dHashtagCounts = postRepo.findPostsUserLiked(viewer.getId(), threeDaysAgo).stream()
            .flatMap(p -> hashtagsListOf(p).stream())
            .collect(Collectors.groupingBy(h -> h, Collectors.counting()));
        Set<String> preferredTags = liked3dHashtagCounts.entrySet().stream()
            .filter(e -> e.getValue() >= 3)
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());

        // 2) dodela bodova po 3 kriterijuma
        Map<Long, Candidate> byIds = facts.stream().collect(Collectors.toMap(Candidate::getPostId, c -> c));
        for (Long pid : candidateIds) {
          Candidate c = byIds.get(pid);
          if (c == null) continue;

          Set<Long> likers = likersByPost.getOrDefault(pid, Set.of());

          // (A) „Dopala se sličnim korisnicima“ => ako je lajkovao BILO KO iz skupa similarUsers
          boolean likedBySimilar = !Collections.disjoint(likers, similarUsers);
          if (likedBySimilar) {
            c.addScore(1, "similarUsersLiked");
             
            
          }
          
   

          // (B) „Slična objavi koju je korisnik lajkovao“ => >=70% preklapanja lajkera
          boolean similarToLiked = isSimilarToAnyUserLiked(pid, viewerLikedIds, likersByPost, 0.70);
          if (similarToLiked) {
            c.addScore(1, "similarPost");
          }

          // (C) „Odgovara preferencama korisnika“ => bar jedan tag iz preferredTags
          boolean matchesPrefs = !Collections.disjoint(preferredTags, c.getHashtags());
          if (matchesPrefs) {
            c.addScore(1, "matchesPreferences");
          }
        }
      }

      
      /*//**********   //*/
      /**/
      facts.forEach(c ->
      System.out.println("[FEED] post="+c.getPostId()
          + " score=" + c.getScore()
          + " reasons=" + c.getReasons()));

      /**/
      
      
    } finally {
      ks.dispose();
    }

    // 7) top 20 po score, zatim novije
   List<Long> topIds = facts.stream()
       // .sorted(Comparator.comparingInt(Candidate::getScore).reversed()
         //   .thenComparing(Candidate::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
		   .sorted(
				      Comparator.comparingInt(Candidate::getScore).reversed()
				        .thenComparing(Candidate::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
				        .thenComparing(Candidate::getPostId) // opciono: stabilan redosled kad je sve isto
				  )
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
        hashtagsListOf(p),
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
  private static List<String> hashtagsListOf(Post p) {
    // Pokušaj da pročitaš kolekciju iz getHashtags()
    try {
      Object raw = Post.class.getMethod("getHashtags").invoke(p);
      if (raw instanceof Collection<?> col) {
        return col.stream()
            .map(o -> {
              if (o == null) return null;
              if (o instanceof String s) return s;
              // pretpostavimo entitet Hashtag sa getName() ili getTag()
              try {
                var m = o.getClass().getMethod("getName");
                Object v = m.invoke(o);
                if (v != null) return v.toString();
              } catch (Exception ignored) {}
              try {
                var m = o.getClass().getMethod("getTag");
                Object v = m.invoke(o);
                if (v != null) return v.toString();
              } catch (Exception ignored) {}
              // kao poslednja opcija:
              return o.toString();
            })
            .filter(Objects::nonNull)
            .distinct()
            .toList();
      }
    } catch (Exception ignored) {}

    // Fallback: CSV polje npr. getHashtagsCsv()
    try {
      String csv = (String) Post.class.getMethod("getHashtagsCsv").invoke(p);
      if (csv == null || csv.isBlank()) return List.of();
      return Arrays.stream(csv.split("[,#\\s]+"))
          .filter(s -> !s.isBlank())
          .map(String::toLowerCase)
          .distinct()
          .toList();
    } catch (Exception ignored) {}

    return List.of();
  }

  

  private static int likesOf(Post p) {
    try { return (int) Post.class.getMethod("getLikes").invoke(p); }
    catch (Exception ignored) {}
    try { Integer x = (Integer) Post.class.getMethod("getLikeCount").invoke(p); return x==null?0:x; }
    catch (Exception ignored) {}
    return 0;
  }
 

  private static String safe(String s){ return s==null? "": s; }
  
  /*///////////////////// */
  
  /** Na osnovu mape {post -> likeri} računa skup korisnika sličnih posmatraču po Pearson korelaciji (binarnim lajkovima). */
  private Set<Long> computeSimilarUsers(Long viewerId,
                                        Map<Long, Set<Long>> likersByPost,
                                        double threshold) {
    // izvučemo sve korisnike koji se pojavljuju kao lajk
    Set<Long> allUsers = new HashSet<>();
    likersByPost.values().forEach(allUsers::addAll);
    allUsers.remove(viewerId);

    // koje je postove lajkovao posmatrač
    Set<Long> viewerVector = likersByPost.entrySet().stream()
        .filter(e -> e.getValue().contains(viewerId))
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());

    Set<Long> result = new HashSet<>();
    for (Long other : allUsers) {
      Set<Long> otherVector = likersByPost.entrySet().stream()
          .filter(e -> e.getValue().contains(other))
          .map(Map.Entry::getKey)
          .collect(Collectors.toSet());
// ///////
      // presek (zajednički lajkovani postovi)
      int inter = 0;
      for (Long x : viewerVector) if (otherVector.contains(x)) inter++;

      if (inter < 3) continue;                 // minimalno 3 ista lajka

      double r = pearsonBinary(viewerVector, otherVector);

      // GATED PRIHVATANJE:
      // - standardno: r >= 0.10 i bar 3 ista lajka
      // - fallback: dozvoli blago negativno do -0.40 ako je presek >= 4
      boolean accept = (r >= 0.10) || (inter >= 4 && r >= -0.40);

      if (accept) result.add(other);  
// ///////
     // double corr = pearsonBinary(viewerVector, otherVector);
      //if (corr >= 0.10) result.add(other);
    }
    return result;
  }

  /** Pearson nad binarnim vektorima „lajkovao / nije lajkovao“. */
  private double pearsonBinary(Set<Long> aLikes, Set<Long> bLikes) {
	    Set<Long> universe = new HashSet<>(aLikes);
	    universe.addAll(bLikes);
	    if (universe.isEmpty()) return 0.0;

	    int n = universe.size();
	    int sumA = 0, sumB = 0, sumA2 = 0, sumB2 = 0, sumAB = 0;
	    for (Long pid : universe) {
	        int ai = aLikes.contains(pid) ? 1 : 0;
	        int bi = bLikes.contains(pid) ? 1 : 0;
	        sumA += ai;
	        sumB += bi;
	        sumA2 += ai * ai;
	        sumB2 += bi * bi;
	        sumAB += ai * bi;
	    }

	    double num = n * sumAB - (double) sumA * sumB;
	    double denLeft = n * sumA2 - (double) sumA * sumA;
	    double denRight = n * sumB2 - (double) sumB * sumB;
	    double den = Math.sqrt(Math.max(denLeft, 0)) * Math.sqrt(Math.max(denRight, 0));

	    if (den == 0) {
	        // identični lajkovi → tretiraj kao maksimalna sličnost
	        return 1.0;
	    }
	    return num / den;
	    

	}

  /** Dve objave su „slične“ ako se ≥70% korisnika koji su lajkovali jednu nalaze i u listi lajkera druge (proveravamo u oba smera). */
  private boolean isSimilarToAnyUserLiked(Long candidatePostId,
                                          Set<Long> viewerLikedPostIds,
                                          Map<Long, Set<Long>> likersByPost,
                                          double threshold) {
    Set<Long> candLikers = likersByPost.getOrDefault(candidatePostId, Set.of());
    if (candLikers.isEmpty()) return false;

    for (Long likedId : viewerLikedPostIds) {
      Set<Long> likedLikers = likersByPost.getOrDefault(likedId, Set.of());
      if (likedLikers.isEmpty()) continue;

      double aInB = overlapRatio(candLikers, likedLikers); // % cand u liked
      double bInA = overlapRatio(likedLikers, candLikers); // % liked u cand
      if (aInB >= threshold || bInA >= threshold) return true;
    }
    return false;
  }

  private double overlapRatio(Set<Long> A, Set<Long> B) {
    if (A.isEmpty()) return 0.0;
    int inter = 0;
    for (Long x : A) if (B.contains(x)) inter++;
    return (double) inter / A.size();
  }

  
}
