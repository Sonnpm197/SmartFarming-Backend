package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.entity.Article;
import com.son.CapstoneProject.entity.search.GenericClass;
import com.son.CapstoneProject.repository.ArticleRepository;
import com.son.CapstoneProject.repository.searchRepository.HibernateSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/article")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class ArticleController {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private HibernateSearchRepository hibernateSearchRepository;

    private static final int ARTICLES_PER_PAGE = 5;

    @GetMapping("/viewNumberOfArticles")
    public long viewNumberOfArticles() {
        return articleRepository.count();
    }

    @GetMapping("/viewArticles/{pageNumber}")
    public Page<Article> viewArticles(@PathVariable int pageNumber) {
        PageRequest pageNumWithElements = PageRequest.of(pageNumber, ARTICLES_PER_PAGE, Sort.by("utilTimestamp"));
        return articleRepository.findAll(pageNumWithElements);
    }

    @GetMapping("/viewArticle/{id}")
    public Article viewArticle(@PathVariable Long id) throws Exception {
        return articleRepository.findById(id)
                .orElseThrow(() -> new Exception("Not found"));
    }

    @GetMapping("/searchArticles/{textSearch}")
    public List<Article> searchArticles(@PathVariable String textSearch) {
        return (List<Article>) hibernateSearchRepository.search2(
                textSearch,
                new GenericClass(Article.class),
                new String[]{"title", "content"}
        );
    }
}
