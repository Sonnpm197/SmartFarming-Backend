package com.son.CapstoneProject.controller;

import com.son.CapstoneProject.configuration.HttpRequestResponseUtils;
import com.son.CapstoneProject.entity.search.GenericClass;
import com.son.CapstoneProject.entity.Question;
import com.son.CapstoneProject.repository.QuestionRepository;
import com.son.CapstoneProject.repository.searchRepository.HibernateSearchRepository;
import com.son.CapstoneProject.service.ViewCountingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/forum")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class ForumController {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ViewCountingService countingService;

    // This repository is for search question by Lucene
    @Autowired
    private HibernateSearchRepository hibernateSearchRepository;

    private static final int QUESTIONS_PER_PAGE = 2;

//    @GetMapping("/viewAllQuestions")
//    public List<Question> viewAllQuestions() {
//        return questionRepository.findAll();
//    }

    @GetMapping("/viewQuestions/{pageNumber}")
    public Page<Question> viewQuestions(@PathVariable int pageNumber) {
        PageRequest pageNumWithElements = PageRequest.of(pageNumber, QUESTIONS_PER_PAGE, Sort.by("utilTimestamp"));
        return questionRepository.findAll(pageNumWithElements);
    }

    @GetMapping("/viewQuestion/{id}")
    public Question viewQuestion(@PathVariable Long id, HttpServletRequest request) throws Exception {
        String ipAddress = HttpRequestResponseUtils.getClientIpAddressIfServletRequestExist(request);
        // Execute asynchronously
        countingService.countView(id, ipAddress);
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
