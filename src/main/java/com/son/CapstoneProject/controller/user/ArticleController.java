package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.configuration.HttpRequestResponseUtils;
import com.son.CapstoneProject.entity.Article;
import com.son.CapstoneProject.entity.search.GenericClass;
import com.son.CapstoneProject.repository.ArticleRepository;
import com.son.CapstoneProject.repository.searchRepository.HibernateSearchRepository;
import com.son.CapstoneProject.service.ViewCountingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.son.CapstoneProject.common.ConstantValue.ARTICLE;
import static com.son.CapstoneProject.common.ConstantValue.ARTICLES_PER_PAGE;

@RestController
@RequestMapping("/article")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class ArticleController {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private HibernateSearchRepository hibernateSearchRepository;

    @Autowired
    private ViewCountingService countingService;

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
    public Article viewArticle(@PathVariable Long id, HttpServletRequest request) throws Exception {
        String ipAddress = HttpRequestResponseUtils.getClientIpAddress(request);
        // Execute asynchronously
        countingService.countView(id, ipAddress, ARTICLE);
        return articleRepository.findById(id)
                .orElseThrow(() -> new Exception("Not found"));
    }

    @GetMapping("/searchArticles/{category}/{textSearch}")
    public List<Article> searchArticles(@PathVariable String category, @PathVariable String textSearch) {
        return (List<Article>) hibernateSearchRepository.search2(
                textSearch,
                ARTICLE,
                new String[]{"title", "content"},
                category
        );
    }
}
