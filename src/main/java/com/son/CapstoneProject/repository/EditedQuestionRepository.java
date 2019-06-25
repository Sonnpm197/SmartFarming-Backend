package com.son.CapstoneProject.repository;

import com.son.CapstoneProject.entity.EditedQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EditedQuestionRepository extends JpaRepository<EditedQuestion, Long> {
}
