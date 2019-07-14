package com.son.CapstoneProject.controller.admin;

import com.son.CapstoneProject.configuration.HttpRequestResponseUtils;
import com.son.CapstoneProject.controller.ControllerUtils;
import com.son.CapstoneProject.entity.Article;
import com.son.CapstoneProject.entity.Tag;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.entity.search.ArticleSearch;
import com.son.CapstoneProject.entity.search.GenericClass;
import com.son.CapstoneProject.repository.ArticleRepository;
import com.son.CapstoneProject.repository.searchRepository.HibernateSearchRepository;
import com.son.CapstoneProject.service.ViewCountingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private ControllerUtils controllerUtils;

    @GetMapping("/viewNumberOfArticles")
    public long viewNumberOfArticles() {
        return articleRepository.count();
    }

    @GetMapping("/viewArticles/{pageNumber}")
    public List<Article> viewArticles(@PathVariable int pageNumber) {
        PageRequest pageNumWithElements = PageRequest.of(pageNumber, ARTICLES_PER_PAGE, Sort.by("utilTimestamp").descending());
        Page<Article> articlePage = articleRepository.findAll(pageNumWithElements);
        return articlePage.getContent();
    }

    @GetMapping("/viewArticle/{id}")
    public Article viewArticle(@PathVariable Long id, HttpServletRequest request) throws Exception {
        String ipAddress = HttpRequestResponseUtils.getClientIpAddress(request);
        // Execute asynchronously
        countingService.countView(id, ipAddress, ARTICLE);
        return articleRepository.findById(id)
                .orElseThrow(() -> new Exception("ArticleController.viewArticle: Not found any article with id: " + id));
    }

    @PostMapping("/searchArticles")
    public List<Article> searchArticles(@RequestBody ArticleSearch articleSearch) {
//        return (List<Article>) hibernateSearchRepository.search2(
//                articleSearch.getTextSearch(),
//                ARTICLE,
//                new String[]{"title", "content"},
//                articleSearch.getCategory()
//        );

        // TODO: finish this
        return null;
    }

    /**
     * Admins can add a new article
     * ** Tag from articles do not count any points to admins
     *
     * @param article
     * @return
     */
    @Transactional
    @PostMapping(value = "/addArticle",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Article> addArticle(@RequestBody Article article) throws Exception {
        String methodName = "ArticleController.addArticle";

        AppUser appUser = article.getAppUser();

        controllerUtils.validateAppUser(appUser, methodName, true);

        article.setUtilTimestamp(new Date());

        // Save tags first (distinctive name)
        List<Tag> tags = controllerUtils.saveDistinctiveTags(article.getTags());
        article.setTags(tags);

        return ResponseEntity.ok(articleRepository.save(article));
    }

    /**
     * Admins can update an article
     *
     * @param updatedArticle
     * @return
     * @throws Exception
     */
    @PutMapping("/updateArticle/{articleId}")
    @Transactional
    public ResponseEntity<Article> updateArticle(
            @RequestBody Article updatedArticle,
            @PathVariable Long articleId)
            throws Exception {
        String methodName = "ArticleController.updateArticle";

        Article oldArticle = articleRepository.findById(articleId)
                .orElseThrow(() -> new Exception(methodName + ": Not found any article with id: " + articleId));

        // Save tags first
        List<Tag> tags = controllerUtils.saveDistinctiveTags(updatedArticle.getTags());

        // Update values
        oldArticle.setTitle(updatedArticle.getTitle());
        oldArticle.setContent(updatedArticle.getContent());
        oldArticle.setTags(tags);
        oldArticle.setUploadedFiles(updatedArticle.getUploadedFiles());
        oldArticle.setUtilTimestamp(new Date());

        // Save to database
        Article question = articleRepository.save(oldArticle);
        return ResponseEntity.ok(question);
    }

    /**
     * Admins can delete an article
     *
     * @param id
     * @return
     * @throws Exception
     */
    @DeleteMapping("/deleteArticle/{id}")
    public Map<String, String> deleteArticle(@PathVariable Long id) throws Exception {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new Exception("ArticleController.deleteArticle: Not found any article with id: " + id));

        // Delete article
        articleRepository.delete(article);

        Map<String, String> map = new HashMap<>();
        map.put("articleId", ("" + id));
        map.put("deleted", "true");
        return map;
    }
}
