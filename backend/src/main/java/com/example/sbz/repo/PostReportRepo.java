package com.example.sbz.repo;
import com.example.sbz.model.PostReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
public interface PostReportRepo extends JpaRepository<PostReport, Long> {
  @Query("select pr from PostReport pr where pr.reportedAt >= :after")
  List<PostReport> findAllAfter(@Param("after") Instant after);
}