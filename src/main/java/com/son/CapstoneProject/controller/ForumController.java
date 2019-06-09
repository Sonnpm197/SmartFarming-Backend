package com.son.CapstoneProject.controller;

import com.son.CapstoneProject.domain.Question;
import com.son.CapstoneProject.repository.QuestionRepository;
import com.son.CapstoneProject.repository.SearchRepository.QuestionSearchRepository;
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
    private QuestionSearchRepository questionSearchRepository;

    @RequestMapping("/hi")
    public @ResponseBody String hiThere(){
        return "hello world!";
    }

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
