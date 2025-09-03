package com.example.sbz.repo;
import com.example.sbz.model.Suspension;
import com.example.sbz.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
public interface SuspensionRepo extends JpaRepository<Suspension, Long> {
  List<Suspension> findByUserAndEndAtAfter(User user, Instant now);
}