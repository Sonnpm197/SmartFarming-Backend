package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.entity.Question;
import com.son.CapstoneProject.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface QuestionRepository extends PagingAndSortingRepository<Question, Long> {

    Page<Question> findAll(Pageable pageable);

    Page<Question> findByAppUser_UserId(Long userId, Pageable pageable);

    List<Question> findByAppUser_UserId(Long userId);

    List<Question> findTop5ByAppUser_UserIdOrderByViewCountDesc(Long userId);

    List<Question> findTop5ByAppUser_UserIdOrderByUpvoteCountDesc(Long userId);

    List<Question> findTop10ByOrderByUpvoteCountDesc();

    List<Question> findTop10ByOrderByUtilTimestampDesc();

    List<Question> findTop10ByOrderByViewCountDesc();

    @Query("select sum(q.viewCount) from Question q")
    Integer getTotalViewCount();

    List<Question> findTop3ByOrderByViewCountDesc();

    @Query("select q from Question q where q.utilTimestamp >= :startDateTime and q.utilTimestamp <= :endDateTime")
    List<Question> findAllByUtilTimestampBetween(@Param("startDateTime") Date startDateTime, @Param("endDateTime") Date endDateTime);

//    @Query("select q from Tag t inner join t.questions q where t = :tag")
//    Page<Question> findBy(@Param("tag") Tag tag, Pageable pageable);

    // For paging purpose
    Page<Question> findByTags_tagId(Long tagId, Pageable pageable);

    // Find full to count pages
    List<Question> findByTags_tagId(Long tagId);
}
