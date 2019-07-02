package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.entity.Question;
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

    List<Question> findByAppUser_UserId(Long userId);

    @Query("select sum(q.viewCount) from Question q")
    int getTotalViewCount();

    @Query("select q from Question q where q.utilTimestamp >= :startDateTime and q.utilTimestamp <= :endDateTime")
    List<Question> findAllByUtilTimestampBetween(@Param("startDateTime") Date startDateTime, @Param("endDateTime") Date endDateTime);
}
