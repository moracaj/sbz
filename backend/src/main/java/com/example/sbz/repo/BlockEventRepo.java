package com.example.sbz.repo;
import com.example.sbz.model.BlockEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
public interface BlockEventRepo extends JpaRepository<BlockEvent, Long> {
  @Query("select b from BlockEvent b where b.createdAt  >= :after")
  List<BlockEvent> findAllAfter(@Param("after") Instant after);
}