package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.entity.Comment;
import com.son.CapstoneProject.entity.Question;
import com.son.CapstoneProject.entity.login.AppUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface CommentRepository extends PagingAndSortingRepository<Comment, Long> {

    List<Comment> findByAppUser_UserId(Long userId);

    List<Comment> findByAnswer_AnswerId(Long answerId);

    List<Comment> findByArticle_ArticleId(Long articleId);

    @Query("select q from Comment q where q.appUser = :appUser and q.utilTimestamp >= :startDateTime and q.utilTimestamp <= :endDateTime")
    List<Comment> findByAppUserAndUtilTimestampBetween(@Param("appUser") AppUser appUser, @Param("startDateTime") Date startDateTime, @Param("endDateTime") Date endDateTime);

    @Query("select q.utilTimestamp from Comment q where q.appUser = :appUser")
    List<Date> findUtilTimeStampByAppUser(@Param("appUser") AppUser appUser);

    @Query("select sum(q.upvoteCount) from Comment q where q.utilTimestamp >= :startDateTime and q.utilTimestamp <= :endDateTime")
    Integer findTotalUpvoteOfCommentsByUtilTimestampBetween(@Param("startDateTime") Date startDateTime, @Param("endDateTime") Date endDateTime);

    @Query(
            value = "select sum(q.upvote_count) from Comment q where year(q.util_timestamp) = :yearParam and month(q.util_timestamp) = :monthParam",
            nativeQuery=true
    )
    Integer findTotalUpvoteOfCommentsByYearAndMonth(@Param("yearParam") int yearParam, @Param("monthParam") int monthParam);

    @Query(
            value = "select sum(q.upvote_count) from Comment q where year(q.util_timestamp) = :yearParam",
            nativeQuery=true
    )
    Integer findTotalUpvoteOfCommentsByYear(@Param("yearParam") int yearParam);
}