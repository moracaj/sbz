package com.example.sbz.repo;
import com.example.sbz.model.Rating;
import com.example.sbz.model.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface RatingRepo extends JpaRepository<Rating, Long> {
  List<Rating> findByPlace(Place place);
}