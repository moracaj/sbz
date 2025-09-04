package com.example.sbz.web;

import com.example.sbz.model.User;
import com.example.sbz.repo.UserRepo;
import com.example.sbz.web.dto.MessageDto;
import com.example.sbz.web.dto.UserSummaryDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserRepo userRepo;

  public UserController(UserRepo userRepo) {
    this.userRepo = userRepo;
  }

  private User me(Authentication auth){
    String email = (String) auth.getPrincipal(); // JwtAuthFilter stavlja email kao principal
    return userRepo.findByEmail(email).orElseThrow();
  }

  @GetMapping("/search")
  public List<UserSummaryDto> search(@RequestParam("q") String q, Authentication auth){
    User me = me(auth);
    var res = userRepo
      .findTop20ByFirstNameIgnoreCaseContainingOrLastNameIgnoreCaseContainingOrEmailIgnoreCaseContaining(q, q, q)
      .stream()
      .filter(u -> !u.getId().equals(me.getId()))
      .map(u -> new UserSummaryDto(
          u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(),
          me.getFriends().contains(u)
      ))
      .toList();
    return res;
  }

  @GetMapping("/me/friends")
  public List<UserSummaryDto> myFriends(Authentication auth){
    User me = me(auth);
    return me.getFriends().stream()
        .map(u -> new UserSummaryDto(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), true))
        .toList();
  }

  @PostMapping("/{id}/friends")
  @Transactional
  public ResponseEntity<MessageDto> addFriend(@PathVariable Long id, Authentication auth){
    User me = me(auth);
    if(me.getId().equals(id)) return ResponseEntity.badRequest().body(new MessageDto("Ne možeš sebe dodati."));

    User target = userRepo.findById(id).orElseThrow();
    if(me.getFriends().contains(target)) {
      return ResponseEntity.ok(new MessageDto("Već ste prijatelji."));
    }

    me.getFriends().add(target);
    target.getFriends().add(me); // simetrično
    userRepo.save(me);
    userRepo.save(target);

    return ResponseEntity.ok(new MessageDto("Prijatelj dodat."));
  }
}
