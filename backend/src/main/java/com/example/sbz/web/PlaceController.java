package com.example.sbz.web;

import com.example.sbz.model.Hashtag;
import com.example.sbz.model.Place;
import com.example.sbz.model.Rating;
import com.example.sbz.model.User;
import com.example.sbz.repo.HashtagRepo;
import com.example.sbz.repo.PlaceRepo;
import com.example.sbz.repo.RatingRepo;
import com.example.sbz.repo.UserRepo;
import com.example.sbz.web.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:5173","http://127.0.0.1:5173"})
@RestController
@RequestMapping("/api/places")
public class PlaceController {

  private final PlaceRepo placeRepo;
  private final RatingRepo ratingRepo;
  private final HashtagRepo hashtagRepo;
  private final UserRepo userRepo;

  public PlaceController(PlaceRepo placeRepo, RatingRepo ratingRepo, HashtagRepo hashtagRepo, UserRepo userRepo) {
    this.placeRepo = placeRepo; this.ratingRepo = ratingRepo; this.hashtagRepo = hashtagRepo; this.userRepo = userRepo;
  }

  // --- helpers ---
  private User me(Authentication auth){
    if (auth == null || auth.getPrincipal() == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No auth");
    String email = Objects.toString(auth.getPrincipal(), null);
    return userRepo.findByEmail(email).orElseThrow();
  }

  private void requireAdmin(User u){
    if (u == null || !u.isAdmin()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
  }

  private List<Hashtag> upsertHashtags(List<String> tags){
    if (tags == null || tags.isEmpty()) return new ArrayList<>();
    List<String> norm = tags.stream()
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(s -> s.startsWith("#") ? s.substring(1) : s)
        .map(String::toLowerCase)
        .distinct()
        .toList();

    if (norm.isEmpty()) return new ArrayList<>();

    List<Hashtag> exist = hashtagRepo.findByTagIn(norm);
    Set<String> have = exist.stream().map(Hashtag::getTag).collect(Collectors.toSet());
    for(String t : norm){
      if (!have.contains(t)) {
        Hashtag h = new Hashtag();
        h.setTag(t);
        exist.add(hashtagRepo.save(h));
      }
    }
    return exist;
  }

  private static List<String> tagsOf(Collection<Hashtag> hs){
    if (hs == null) return List.of();
    return hs.stream().map(Hashtag::getTag).toList();
  }

  private static PlaceDto toDto(Place p){
    var ratings = (p.getRatings() == null) ? List.<Rating>of() : new ArrayList<>(p.getRatings());
    double avg = ratings.isEmpty() ? 0.0 : ratings.stream().mapToInt(Rating::getScore).average().orElse(0.0);
    return new PlaceDto(
        p.getId(), p.getName(), p.getCountry(), p.getCity(), p.getDescription(),
        tagsOf(p.getHashtags()), Math.round(avg * 10.0) / 10.0, ratings.size()
    );
  }

  private static RatingDto toDto(Rating r){
    User a = r.getAuthor();
    String authorName = (a == null) ? "Unknown" : ((a.getFirstName()==null?"":a.getFirstName())+" "+(a.getLastName()==null?"":a.getLastName())).trim();
    return new RatingDto(
        r.getId(),
        a != null ? a.getId() : null,
        authorName,
        r.getScore(),
        r.getComment(),
        tagsOf(r.getHashtags()),
        r.getCreatedAt()
    );
  }

  // --- PUBLIC (pregled) ---


  @GetMapping
  @Transactional(readOnly = true)
  public ResponseEntity<List<PlaceDto>> list(){
    List<Place> places = placeRepo.fetchAll(); // <-- više nema LAZY problema
    return ResponseEntity.ok(places.stream().map(PlaceController::toDto).toList());
  }
  
  @GetMapping("/{id}")
  @Transactional(readOnly = true)
  public ResponseEntity<PlaceDetailsDto> details(@PathVariable("id") Long id){
    Place p = placeRepo.fetchById(id).orElseThrow();
    var base = toDto(p);
    var ratings = p.getRatings().stream().map(PlaceController::toDto).toList();
    return ResponseEntity.ok(new PlaceDetailsDto(base, ratings));
  }

  // --- ADMIN (dodavanje i ocenjivanje) ---

  @PostMapping
  @Transactional
  public ResponseEntity<PlaceDto> create(@RequestBody PlaceCreateRequest req, Authentication auth){
    User u = me(auth);
    requireAdmin(u);

    if (req.name == null || req.name.trim().isEmpty())
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
    if (req.country == null || req.country.trim().isEmpty())
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Country is required");
    if (req.city == null || req.city.trim().isEmpty())
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "City is required");

    Place p = new Place();
    p.setName(req.name.trim());
    p.setCountry(req.country.trim());
    p.setCity(req.city.trim());
    p.setDescription(req.description == null ? "" : req.description.trim());
    p.setHashtags(new HashSet<>(upsertHashtags(req.hashtags)));

    p = placeRepo.save(p);
    return ResponseEntity.status(HttpStatus.CREATED).body(toDto(p));
  }

  @PostMapping("/{id}/ratings")
  @Transactional
  public ResponseEntity<RatingDto> rate(@PathVariable("id") Long id,
                                        @RequestBody RatingCreateRequest req,
                                        Authentication auth){
    User admin = me(auth);
    requireAdmin(admin);

    if (req.score < 1 || req.score > 5)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Score must be 1..5");

    Place p = placeRepo.findById(id).orElseThrow();

    Rating r = new Rating();
    r.setPlace(p);
    r.setAuthor(admin); // samo admin sme (po zahtevu)
    r.setScore(req.score);
    r.setComment(req.comment == null ? "" : req.comment.trim());
    r.setHashtags(new HashSet<>(upsertHashtags(req.hashtags)));
    r.setCreatedAt(Instant.now());

    r = ratingRepo.save(r);
    return ResponseEntity.status(HttpStatus.CREATED).body(toDto(r));
  }
}
