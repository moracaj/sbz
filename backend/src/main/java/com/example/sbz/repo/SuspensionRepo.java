package com.example.sbz.repo;
import com.example.sbz.model.Suspension;
import com.example.sbz.model.SuspensionType;
import com.example.sbz.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;


public interface SuspensionRepo extends JpaRepository<Suspension, Long> {
 
	List<Suspension> findByUserAndEndAtAfter(User user, Instant now);


	 @Query("select s from Suspension s where s.endAt > :now")
	  List<Suspension> findActive(@Param("now") Instant now);

	  @Query("select s from Suspension s where s.endAt > :now and s.type = :type")
	  List<Suspension> findActiveByType(@Param("type") SuspensionType type,
	                                    @Param("now") Instant now);

	  @Modifying
	  @Query("update Suspension s set s.endAt = :now where s.user.id = :userId and s.endAt > :now")
	  int deactivateAllForUser(@Param("userId") Long userId, @Param("now") Instant now);
	}


