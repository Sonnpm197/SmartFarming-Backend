package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.entity.Question;
import com.son.CapstoneProject.entity.Tag;
import com.son.CapstoneProject.entity.login.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

@Repository
public interface QuestionRepository extends PagingAndSortingRepository<Question, Long> {

    List<Question> findAll();

    Page<Question> findAll(Pageable pageable);

    Page<Question> findByAppUser_UserId(Long userId, Pageable pageable);

    List<Question> findByAppUser_UserId(Long userId);

    @Query("select q.utilTimestamp from Question q where q.appUser = :appUser")
    List<Date> findUtilTimeStampByAppUser(@Param("appUser") AppUser appUser);

    List<Question> findTop5ByAppUser_UserIdOrderByViewCountDesc(Long userId);

    List<Question> findTop5ByAppUser_UserIdOrderByUpvoteCountDesc(Long userId);

    List<Question> findTop5ByAppUser_UserIdOrderByUtilTimestampDesc(Long userId);

    List<Question> findTop10ByOrderByUpvoteCountDesc();

    List<Question> findTop10ByOrderByUtilTimestampDesc();

    List<Question> findTop10ByOrderByViewCountDesc();

    @Query("select sum(q.viewCount) from Question q")
    Integer getTotalViewCount();

    List<Question> findTop3ByOrderByViewCountDesc();

    @Query("select q from Question q where q.appUser = :appUser and q.utilTimestamp >= :startDateTime and q.utilTimestamp <= :endDateTime")
    List<Question> findByAppUserAndUtilTimestampBetween(@Param("appUser") AppUser appUser, @Param("startDateTime") Date startDateTime, @Param("endDateTime") Date endDateTime);

//    @Query("select q from Tag t inner join t.questions q where t = :tag")
//    Page<Question> findBy(@Param("tag") Tag tag, Pageable pageable);

    // For paging purpose
    Page<Question> findByTags_tagId(Long tagId, Pageable pageable);

    @Query(
            value = "select distinct q.question_id from question q join question_tags qts\n" +
                    "on q.question_id = qts.questions_question_id\n" +
                    "where qts.tags_tag_id in :tagId",
            nativeQuery = true
    )
    List<BigInteger> findDistinctByTags_tagIdIn(@Param("tagId") List<Long> tagId);

    // Count number of question by tagId
    @Query(
            value = "select count(distinct q.question_id) from question q join question_tags qts\n" +
                    "on q.question_id = qts.questions_question_id\n" +
                    "where qts.tags_tag_id in :tagId",
            nativeQuery = true
    )
    Integer countDistinctNumberOfQuestionsByTags_tagIdIn(@Param("tagId") List<Long> tagId);

    // Count number of question by tagId
    @Query(
            value = "select count(q.question_id) from question q join question_tags qt\n" +
                    "                        on q.question_id = qt.questions_question_id\n" +
                    "where qt.tags_tag_id = :tagId",
            nativeQuery = true
    )
    Integer countNumberOfQuestionsByTagId(@Param("tagId") Long tagId);

    @Query("select sum(q.viewCount) from Question q where q.utilTimestamp >= :startDateTime and q.utilTimestamp <= :endDateTime")
    Integer findTotalViewOfQuestionsByUtilTimestampBetween(@Param("startDateTime") Date startDateTime, @Param("endDateTime") Date endDateTime);

    @Query("select sum(q.upvoteCount) from Question q where q.utilTimestamp >= :startDateTime and q.utilTimestamp <= :endDateTime")
    Integer findTotalUpvoteOfQuestionsByUtilTimestampBetween(@Param("startDateTime") Date startDateTime, @Param("endDateTime") Date endDateTime);

    @Query(
            value = "select sum(q.view_count) from Question q where year(q.util_timestamp) = :yearParam and month(q.util_timestamp) = :monthParam",
            nativeQuery = true
    )
    Integer findTotalViewOfQuestionsByYearAndMonth(@Param("yearParam") int yearParam, @Param("monthParam") int monthParam);

    @Query(
            value = "select sum(q.upvote_count) from Question q where year(q.util_timestamp) = :yearParam and month(q.util_timestamp) = :monthParam",
            nativeQuery = true
    )
    Integer findTotalUpvoteOfQuestionsByYearAndMonth(@Param("yearParam") int yearParam, @Param("monthParam") int monthParam);

    @Query(
            value = "select sum(q.view_count) from Question q where year(q.util_timestamp) = :yearParam",
            nativeQuery = true
    )
    Integer findTotalViewOfQuestionsByYear(@Param("yearParam") int yearParam);

    @Query(
            value = "select sum(q.upvote_count) from Question q where year(q.util_timestamp) = :yearParam",
            nativeQuery = true
    )
    Integer findTotalUpvoteOfQuestionsByYear(@Param("yearParam") int yearParam);

    @Query(
            value = "select count(q.question_id) from question q where q.user_id = :userId",
            nativeQuery = true
    )
    Integer countNumberOfQuestionsByUserId(@Param("userId") Long userId);

    List<Question> findTop5ByTags_tagIdAndQuestionIdNotInOrderByViewCountDescUpvoteCountDesc(Long tagId, List<Long> questionIds);
}
