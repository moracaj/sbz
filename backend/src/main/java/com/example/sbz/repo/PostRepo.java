package com.example.sbz.repo;

import com.example.sbz.model.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.Instant;
import java.util.Collection;

public interface PostRepo extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = { "hashtags", "author", "likedBy"}) // 👈 učitaj kolekcije zajedno sa postovima
    @Query("""
    		select p from Post p
           where p.author.id = :authorId
           order by p.createdAt desc
           """)
    List<Post> findByAuthorIdOrderByCreatedAtDesc(@Param("authorId") Long authorId);

    
 // Poslednja 24h od prijatelja
    @Query("""
      select p from Post p
      where p.author.id in :authorIds and p.createdAt >= :after
      order by p.createdAt desc
    """)
    List<Post> recentFromAuthors(@Param("authorIds") Collection<Long> authorIds,
                                 @Param("after") Instant after);

    // Kandidati koji NISU prijatelji (i nisu ja), poslednjih X dana
    @Query("""
      select p from Post p
      where p.author.id not in :excludeIds and p.createdAt >= :after
      order by p.createdAt desc
    """)
    List<Post> recentNotFromAuthors(@Param("excludeIds") Collection<Long> excludeIds,
                                    @Param("after") Instant after);

    // Za popularne heštegove – skeniramo sve postove u poslednjih 24h
    @Query("""
      select p from Post p
      where p.createdAt >= :after
    """)
    List<Post> recentAll(@Param("after") Instant after);


}
