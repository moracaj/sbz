package com.example.sbz.web.dto;

public class RegisterRequest {
  private String firstName;
  private String lastName;
  private String email;
  private String password;
  private Long homePlaceId; // mesto stanovanja (Place.id)

  // getters/setters
  public String getFirstName() { return firstName; }
  public void setFirstName(String firstName) { this.firstName = firstName; }
  public String getLastName() { return lastName; }
  public void setLastName(String lastName) { this.lastName = lastName; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }
  public Long getHomePlaceId() { return homePlaceId; }
  public void setHomePlaceId(Long homePlaceId) { this.homePlaceId = homePlaceId; }
}
