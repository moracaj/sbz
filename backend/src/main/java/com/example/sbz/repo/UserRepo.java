package com.example.sbz.repo;

import com.example.sbz.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepo extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  List<User> findTop20ByFirstNameIgnoreCaseContainingOrLastNameIgnoreCaseContainingOrEmailIgnoreCaseContaining(
      String q1, String q2, String q3);

  // ⚠️ OVO JE NOVO: eksplicitni fetch prijatelja preko JPQL
  @Query("select u from User u left join fetch u.friends where u.email = :email")
  Optional<User> findByEmailFetchFriends(@Param("email") String email);

  // ID-jevi prijatelja - jeftino za “da li je već prijatelj”
  @Query("select f.id from User u join u.friends f where u.id = :meId")
  Set<Long> findFriendIds(@Param("meId") Long meId);

  // Lista prijatelja za /me/friends (fetch)
  @Query("select f from User u join u.friends f where u.id = :meId")
  List<User> findFriendsOf(@Param("meId") Long meId);
  
  
  @Modifying
  @Query(
    value = """
      INSERT INTO user_friends (user_id, friend_id)
      SELECT :a, :b
      WHERE NOT EXISTS (
        SELECT 1 FROM user_friends WHERE user_id = :a AND friend_id = :b
      )
    """,
    nativeQuery = true
  )
  int linkIfAbsent(@Param("a") Long a, @Param("b") Long b);
  
  
  
  
  @Modifying
  @Query(
    value = """
      INSERT INTO user_blocks (blocker_id, blocked_id)
      SELECT :a, :b
      WHERE NOT EXISTS (
        SELECT 1 FROM user_blocks WHERE blocker_id = :a AND blocked_id = :b
      )
    """,
    nativeQuery = true
  )
  int linkBlockIfAbsent(@Param("a") Long a, @Param("b") Long b);

  @Modifying
  @Query(value = "DELETE FROM user_blocks WHERE blocker_id=:a AND blocked_id=:b", nativeQuery = true)
  int unlinkBlock(@Param("a") Long a, @Param("b") Long b);

  @Query(value = "SELECT COUNT(*) FROM user_blocks WHERE blocker_id=:a AND blocked_id=:b", nativeQuery = true)
  long isBlockedAB(@Param("a") Long a, @Param("b") Long b);

  @Query(value = "SELECT blocked_id FROM user_blocks WHERE blocker_id=:me", nativeQuery = true)
  List<Long> findBlockedIds(@Param("me") Long me);

  // koristi se kad blokiraš da “razdružiš” ako su već prijatelji
  @Modifying
  @Query(value = "DELETE FROM user_friends WHERE user_id=:a AND friend_id=:b", nativeQuery = true)
  int unlinkFriend(@Param("a") Long a, @Param("b") Long b);
  

  
  
}
