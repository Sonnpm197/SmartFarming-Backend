package com.son.CapstoneProject.controller;

import com.son.CapstoneProject.domain.Article;
import com.son.CapstoneProject.repository.ArticleRepository;
import com.son.CapstoneProject.repository.SearchRepository.ArticleSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/article")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class ArticleController {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ArticleSearchRepository articleSearchRepository;

    @GetMapping("/viewAllArticles")
    public List<Article> viewAllArticles() {
        return articleRepository.findAll();
    }

    @GetMapping("/viewQuestion/{id}")
    public Article viewArticle(@PathVariable Long id) throws Exception {
        return articleRepository.findById(id)
                .orElseThrow(() -> new Exception("Not found"));
    }

    @GetMapping("/searchArticles/{textSearch}")
    public List<Article> searchArticles(@PathVariable String textSearch) {
        return articleSearchRepository.searchArticles(textSearch);
    }
}
