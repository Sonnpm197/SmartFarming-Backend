package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.entity.Answer;
import com.son.CapstoneProject.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByAppUser_UserId(Long userId);

    @Query("select q from Answer q where q.utilTimestamp >= :startDateTime and q.utilTimestamp <= :endDateTime")
    List<Answer> findAllByUtilTimestampBetween(@Param("startDateTime") Date startDateTime, @Param("endDateTime") Date endDateTime);

}
