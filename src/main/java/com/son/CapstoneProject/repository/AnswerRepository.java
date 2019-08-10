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

    @Query("select q.utilTimestamp from Answer q where q.appUser = :appUser")
    List<Date> findUtilTimeStampByAppUser(@Param("appUser") AppUser appUser);

    @Query("select sum(q.upvoteCount) from Answer q where q.utilTimestamp >= :startDateTime and q.utilTimestamp <= :endDateTime")
    Integer findTotalUpvoteOfAnswersByUtilTimestampBetween(@Param("startDateTime") Date startDateTime, @Param("endDateTime") Date endDateTime);

    @Query(
            value = "select sum(q.upvote_count) from Answer q where year(q.util_timestamp) = :yearParam and month(q.util_timestamp) = :monthParam",
            nativeQuery=true
    )
    Integer findTotalUpvoteOfAnswersByYearAndMonth(@Param("yearParam") int yearParam, @Param("monthParam") int monthParam);

    @Query(
            value = "select sum(q.upvote_count) from Answer q where year(q.util_timestamp) = :yearParam",
            nativeQuery=true
    )
    Integer findTotalUpvoteOfAnswersByYear(@Param("yearParam") int yearParam);
}
