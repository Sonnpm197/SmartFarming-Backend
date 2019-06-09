package com.son.CapstoneProject.controller;

import com.son.CapstoneProject.domain.Article;
import com.son.CapstoneProject.domain.Question;
import com.son.CapstoneProject.repository.ArticleRepository;
import com.son.CapstoneProject.repository.SearchRepository.ArticleSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ArticleSearchRepository articleSearchRepository;

    @GetMapping("/viewAllArticles")
    public List<Article> viewAllArticles() {
        return articleRepository.findAll();
    }

    @GetMapping("/searchArticles/{textSearch}")
    public List<Article> searchArticles(@PathVariable String textSearch) {
        return articleSearchRepository.searchArticles(textSearch);
    }
}
