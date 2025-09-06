package com.example.sbz.repo;
import com.example.sbz.model.Place;



import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface PlaceRepo extends JpaRepository<Place, Long> {
  List<Place> findByCityIgnoreCase(String city);
  List<Place> findByCountryIgnoreCase(String country);
  
  @EntityGraph(attributePaths = {"hashtags", "ratings"})
  @Query("select p from Place p")
  List<Place> fetchAll();  // umesto plain findAll()

  @EntityGraph(attributePaths = {"hashtags", "ratings", "ratings.hashtags", "ratings.author"})
  @Query("select p from Place p where p.id = :id")
  Optional<Place> fetchById(@Param("id") Long id);
}