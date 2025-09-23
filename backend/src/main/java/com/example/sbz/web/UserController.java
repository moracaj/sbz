package com.example.sbz.web;

import com.example.sbz.model.User;
import com.example.sbz.repo.UserRepo;
import com.example.sbz.web.dto.MessageDto;
import com.example.sbz.web.dto.UserSummaryDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.example.sbz.model.BlockEvent;
import com.example.sbz.repo.BlockEventRepo;
import java.time.Instant;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173","http://127.0.0.1:5173"})
@RestController
@RequestMapping("/api/users")
 public class UserController {
   private final UserRepo userRepo;
   private final BlockEventRepo blockEventRepo;
   public UserController(UserRepo userRepo, BlockEventRepo blockEventRepo) {
     this.userRepo = userRepo; this.blockEventRepo = blockEventRepo;
   }
  
private String usernameOf(Authentication auth) {
  var p = auth.getPrincipal();
  if (p instanceof org.springframework.security.core.userdetails.UserDetails ud) {
    return ud.getUsername();
  }
  return String.valueOf(p);
}
  
private User meWithFriends(Authentication auth) {
	  String email = usernameOf(auth);
	  return userRepo.findByEmailFetchFriends(email).orElseThrow(); // ⬅️ NOVO
	}
  
  
  private User me(Authentication auth) {
	  String email = usernameOf(auth);
	  return userRepo.findByEmail(email).orElseThrow();
	}

  
  @GetMapping("/search")
  @Transactional(readOnly = true)
  public List<UserSummaryDto> search(@RequestParam("q") String q, Authentication auth){
    User me = me(auth);
    var friendIds = userRepo.findFriendIds(me.getId());  
   // var blockedIds=userRepo.findFriendIds(me.getId());  // ⬅️ NOVO
    return userRepo
      .findTop20ByFirstNameIgnoreCaseContainingOrLastNameIgnoreCaseContainingOrEmailIgnoreCaseContaining(q, q, q)
      .stream()
      .filter(u -> !u.getId().equals(me.getId()))
      .map(u -> new UserSummaryDto(
          u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(),
          friendIds.contains(u.getId()),  false                             // ⬅️ bez LAZY contains()
      ))
      .toList();
  }

  @GetMapping("/me/friends")
  @Transactional(readOnly = true)
  public List<UserSummaryDto> myFriends(Authentication auth){
    User me = me(auth);
    var friends = userRepo.findFriendsOf(me.getId());              // ⬅️ NOVO
    return friends.stream()
        .map(u -> new UserSummaryDto(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), true, false))
        .toList();
  }
  
  
  @PostMapping("/{id}/friends")
  @Transactional
  public ResponseEntity<MessageDto> addFriend(@PathVariable("id") Long id, Authentication auth){
    User me = me(auth);
    if (me.getId().equals(id)) {
      return ResponseEntity.badRequest().body(new MessageDto("Ne možeš sebe dodati."));
    }

    // validiraj da target postoji (da uhvatimo 404 kao normalan slučaj)
    userRepo.findById(id).orElseThrow();

    if (userRepo.isBlockedAB(me.getId(), id) > 0 || userRepo.isBlockedAB(id, me.getId()) > 0) {
        return ResponseEntity.badRequest().body(new MessageDto("Dodavanje nije moguće: korisnik je blokiran."));
      }
    
    // upiši oba smera u join tabelu, bez duplikata
    int a = userRepo.linkIfAbsent(me.getId(), id);
    int b = userRepo.linkIfAbsent(id, me.getId());

    if (a == 0 && b == 0) {
      return ResponseEntity.ok(new MessageDto("Već ste prijatelji."));
    }
    return ResponseEntity.ok(new MessageDto("Prijatelj dodat."));
  }
  
  
  
  
  @GetMapping("/me/blocked")
  public List<UserSummaryDto> myBlocked(Authentication auth){
    User me = me(auth);
    List<Long> ids = userRepo.findBlockedIds(me.getId());
    if(ids.isEmpty()) return List.of();
    List<User> users = userRepo.findAllById(ids);
    return users.stream().map(u -> new UserSummaryDto(
        u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(),
        false /*friend*/, true /*blocked*/
    )).toList();
  }

  @PostMapping("/{id}/block")
  @Transactional
  public ResponseEntity<MessageDto> block(@PathVariable("id") Long id, Authentication auth){
    User me = me(auth);
    if(me.getId().equals(id)){
      return ResponseEntity.badRequest().body(new MessageDto("Ne možeš blokirati sebe."));
    }
    userRepo.findById(id).orElseThrow();

    // ako su prijatelji – razdruži oba smera
    userRepo.unlinkFriend(me.getId(), id);
    userRepo.unlinkFriend(id, me.getId());

    int x = userRepo.linkBlockIfAbsent(me.getId(), id);
    if (x > 0) { // novi blok – upiši događaj
	      BlockEvent ev = new BlockEvent();
	      ev.setTarget(userRepo.findById(id).orElseThrow());
	      ev.setByUser(me);
	      ev.setCreatedAt(Instant.now());
	      blockEventRepo.save(ev);
	      // (po želji) razdruži ako su prijatelji – već imaš unlinkFriend u repo-u
	      userRepo.unlinkFriend(me.getId(), id);
	    }
    return ResponseEntity.ok(new MessageDto(x == 0 ? "Već je blokiran." : "Korisnik blokiran."));
  }

  @DeleteMapping("/{id}/block")
  @Transactional
  public ResponseEntity<MessageDto> unblock(@PathVariable("id") Long id, Authentication auth){
    User me = me(auth);
    userRepo.findById(id).orElseThrow();
    int x = userRepo.unlinkBlock(me.getId(), id);
       
    return ResponseEntity.ok(new MessageDto(x == 0 ? "Nije bio blokiran." : "Deblokiran."));
  }

  
}
