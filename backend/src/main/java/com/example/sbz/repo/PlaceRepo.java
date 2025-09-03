package com.example.sbz.repo;
import com.example.sbz.model.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PlaceRepo extends JpaRepository<Place, Long> {
  List<Place> findByCityIgnoreCase(String city);
  List<Place> findByCountryIgnoreCase(String country);
}