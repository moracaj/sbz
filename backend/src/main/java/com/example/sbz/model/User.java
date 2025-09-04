package com.example.sbz.model;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String firstName;
  private String lastName;

  @Column(nullable=false)
  private String email;

  @Column(nullable=false)
  private String passwordHash;

  private boolean admin;
  private boolean suspendedPost;
  private boolean suspendedLogin;

  private Instant createdAt = Instant.now();

  // Mesto stanovanja kao JPA veza (više korisnika može živeti u istom mestu)
  @ManyToOne
  @JoinColumn(name = "home_place_id")
  private Place homePlace;
  
  
  
  @ManyToMany
  @JoinTable(
    name = "user_friends",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "friend_id")
  )
  private Set<User> friends = new HashSet<>();


  

  // --- get/set ---
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public String getFirstName() { return firstName; }
  public void setFirstName(String firstName) { this.firstName = firstName; }

  public String getLastName() { return lastName; }
  public void setLastName(String lastName) { this.lastName = lastName; }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public String getPasswordHash() { return passwordHash; }
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

  public boolean isAdmin() { return admin; }
  public void setAdmin(boolean admin) { this.admin = admin; }

  public boolean isSuspendedPost() { return suspendedPost; }
  public void setSuspendedPost(boolean suspendedPost) { this.suspendedPost = suspendedPost; }

  public boolean isSuspendedLogin() { return suspendedLogin; }
  public void setSuspendedLogin(boolean suspendedLogin) { this.suspendedLogin = suspendedLogin; }

  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

  public Place getHomePlace() { return homePlace; }
  public void setHomePlace(Place homePlace) { this.homePlace = homePlace; }

  public Set<User> getFriends() { return friends; }
  public void setFriends(Set<User> friends) { this.friends = friends; }

}
