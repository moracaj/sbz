package com.example.sbz.repo;
import com.example.sbz.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;


public interface UserRepo extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);
  
  List<User> findTop20ByFirstNameIgnoreCaseContainingOrLastNameIgnoreCaseContainingOrEmailIgnoreCaseContaining(
	      String q1, String q2, String q3);
}