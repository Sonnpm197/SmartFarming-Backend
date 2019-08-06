package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.entity.Answer;
import com.son.CapstoneProject.entity.Question;
import com.son.CapstoneProject.entity.login.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByAppUser_UserId(Long userId);

    @Query("select q from Answer q where q.appUser = :appUser and q.utilTimestamp >= :startDateTime and q.utilTimestamp <= :endDateTime")
    List<Answer> findByAppUserAndUtilTimestampBetween(@Param("appUser") AppUser appUser, @Param("startDateTime") Date startDateTime, @Param("endDateTime") Date endDateTime);

}
