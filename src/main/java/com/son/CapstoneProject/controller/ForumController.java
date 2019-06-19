package com.son.CapstoneProject.controller;

import com.son.CapstoneProject.entity.GenericClass;
import com.son.CapstoneProject.entity.Question;
import com.son.CapstoneProject.repository.QuestionRepository;
import com.son.CapstoneProject.repository.searchRepository.HibernateSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/forum")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class ForumController {

    @Autowired
    private QuestionRepository questionRepository;

    // This repository is for search question by Lucene
    @Autowired
    private HibernateSearchRepository hibernateSearchRepository;

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
        return (List<Question>) hibernateSearchRepository.search2(
                textSearch,
                new GenericClass(Question.class),
                new String[]{"title", "content"} //  fields
        );
    }

}
