package com.example.sbz.repo;

import com.example.sbz.model.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepo extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = { "hashtags", "author", "likedBy"}) // 👈 učitaj kolekcije zajedno sa postovima
    @Query("""
    		select p from Post p
           where p.author.id = :authorId
           order by p.createdAt desc
           """)
    List<Post> findByAuthorIdOrderByCreatedAtDesc(@Param("authorId") Long authorId);
}
