package com.son.CapstoneProject.controller;

import com.son.CapstoneProject.domain.Article;
import com.son.CapstoneProject.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/article")
@CrossOrigin(origins = "http://localhost:4200")
public class ArticleController {

    @Autowired
    private ArticleRepository articleRepository;

    @GetMapping("/viewAllArticles")
    public List<Article> viewAllArticles() {
        return articleRepository.findAll();
    }
}
