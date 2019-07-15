package com.son.CapstoneProject.controller.admin;

import com.son.CapstoneProject.configuration.HttpRequestResponseUtils;
import com.son.CapstoneProject.controller.ControllerUtils;
import com.son.CapstoneProject.controller.FileController;
import com.son.CapstoneProject.entity.Article;
import com.son.CapstoneProject.entity.Comment;
import com.son.CapstoneProject.entity.Tag;
import com.son.CapstoneProject.entity.UploadedFile;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.entity.pagination.ArticlePagination;
import com.son.CapstoneProject.entity.search.ArticleSearch;
import com.son.CapstoneProject.entity.search.GenericClass;
import com.son.CapstoneProject.repository.ArticleRepository;
import com.son.CapstoneProject.repository.CommentRepository;
import com.son.CapstoneProject.repository.UploadedFileRepository;
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
import java.util.*;

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
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private ControllerUtils controllerUtils;

    @Autowired
    private FileController fileController;

    @Autowired
    private CommentRepository commentRepository;

    @GetMapping("/viewNumberOfArticles")
    public long viewNumberOfArticles() {
        return articleRepository.count();
    }

    @GetMapping("/viewNumberOfPages")
    public long viewNumberOfPages() {
        long numberOfArticle = articleRepository.count();
        if (numberOfArticle % ARTICLES_PER_PAGE == 0) {
            return numberOfArticle / ARTICLES_PER_PAGE;
        } else {
            return (numberOfArticle / ARTICLES_PER_PAGE) + 1;
        }
    }

    @GetMapping("/viewArticles/{pageNumber}")
    public ArticlePagination viewArticles(@PathVariable int pageNumber) {
        PageRequest pageNumWithElements = PageRequest.of(pageNumber, ARTICLES_PER_PAGE, Sort.by("utilTimestamp").descending());
        Page<Article> articlePage = articleRepository.findAll(pageNumWithElements);
        ArticlePagination articlePagination = new ArticlePagination();
        articlePagination.setArticlesByPageIndex(articlePage.getContent());
        articlePagination.setNumberOfPages(Integer.parseInt("" + viewNumberOfPages()));
        return articlePagination;
    }

    @GetMapping("/viewArticle/{id}")
    public Article viewArticle(@PathVariable Long id, HttpServletRequest request) throws Exception {
        String ipAddress = HttpRequestResponseUtils.getClientIpAddress(request);
        // Execute asynchronously
        countingService.countView(id, ipAddress, ARTICLE);
        return articleRepository.findById(id)
                .orElseThrow(() -> new Exception("ArticleController.viewArticle: Not found any article with id: " + id));
    }

    @PostMapping("/searchArticles/{pageNumber}")
    public ArticlePagination searchArticles(@RequestBody ArticleSearch articleSearch, @PathVariable int pageNumber) {
        return (ArticlePagination) hibernateSearchRepository.search2(
                articleSearch.getTextSearch(),
                ARTICLE,
                new String[]{"title", "content"},
                articleSearch.getCategory(),
                pageNumber
        );
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

        article = articleRepository.save(article);

        // Note: this uploaded file are already saved on GG Cloud
        // This requested question will have UploadedFile objects => save info of this question to that UploadedFile
        List<UploadedFile> uploadedFiles = article.getUploadedFiles();

        if (uploadedFiles != null) {
            for (UploadedFile uploadedFile : uploadedFiles) {
                // We still need to save question for this uploaded file
                uploadedFile.setArticle(article);
                uploadedFileRepository.save(uploadedFile);
            }
        }

        return ResponseEntity.ok(article);
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
        oldArticle.setUtilTimestamp(new Date());

        // Delete old images from DB and delete file on google cloud storage
        List<UploadedFile> oldUploadedFiles = oldArticle.getUploadedFiles();
        for (UploadedFile oldUploadedFile: oldUploadedFiles) {
            fileController.deleteFile(oldUploadedFile);
        }

        // This requested article will have UploadedFile objects => save info of this question to that UploadedFile
        List<UploadedFile> newUploadedFiles = updatedArticle.getUploadedFiles();
        oldArticle.setUploadedFiles(newUploadedFiles);

        // Save to database
        Article resultArticle = articleRepository.save(oldArticle);

        // Set article_id for these new uploaded files
        if (newUploadedFiles != null) {
            for (UploadedFile uploadedFile : newUploadedFiles) {
                uploadedFile.setArticle(resultArticle);
                uploadedFileRepository.save(uploadedFile);
            }
        }

        return ResponseEntity.ok(resultArticle);
    }

    /**
     * Admins can delete an article
     *
     * @return
     * @throws Exception
     */
    @DeleteMapping("/deleteArticle/{articleId}")
    public Map<String, String> deleteArticle(@PathVariable Long articleId) throws Exception {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new Exception("ArticleController.deleteArticle: Not found any article with id: " + articleId));

        // Remove the comments
        List<Comment> comments = article.getComments();
        Iterator<Comment> commentIterator = comments.iterator();

        while (commentIterator.hasNext()) {
            Comment comment = commentIterator.next();
            commentRepository.delete(comment);
        }

        // Delete UploadedFile both from GG cloud and DB
        List<UploadedFile> uploadedFiles = article.getUploadedFiles();
        for (UploadedFile uploadedFile: uploadedFiles) {
            fileController.deleteFile(uploadedFile);
        }

        // Delete article
        articleRepository.delete(article);

        Map<String, String> map = new HashMap<>();
        map.put("articleId", ("" + articleId));
        map.put("deleted", "true");
        return map;
    }
}
