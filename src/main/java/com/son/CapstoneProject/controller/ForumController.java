package com.son.CapstoneProject.controller;

import com.son.CapstoneProject.domain.Question;
import com.son.CapstoneProject.repository.QuestionRepository;
import com.son.CapstoneProject.repository.SearchRepository.QuestionSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/forum")
public class ForumController {

    @Autowired
    private QuestionRepository questionRepository;

    // This repository is for search question by Lucene
    @Autowired
    private QuestionSearchRepository questionSearchRepository;

    @GetMapping("/viewAllQuestions")
    public List<Question> viewAllQuestions() {
        return questionRepository.findAll();
    }

    @GetMapping("/viewQuestion/{id}")
    public Question viewQuestion(@PathVariable Long id) throws Exception {
        return questionRepository.findById(id)
                .orElseThrow(() -> new Exception("Not found"));
    }

    @GetMapping("/searchQuestions/{textSearch}")
    public List<Question> searchQuestions(@PathVariable String textSearch) {
        return questionSearchRepository.searchQuestions(textSearch);
    }

}
