package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> getQuestionsByClient_Email(String clientEmail);
}
