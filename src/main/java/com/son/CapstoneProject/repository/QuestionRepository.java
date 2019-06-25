package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends PagingAndSortingRepository<Question, Long> {

    Page<Question> findAll(Pageable pageable);

}
