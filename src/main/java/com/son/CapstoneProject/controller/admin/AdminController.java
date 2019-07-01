package com.son.CapstoneProject.controller.admin;

import com.son.CapstoneProject.controller.ControllerUtils;
import com.son.CapstoneProject.entity.Article;
import com.son.CapstoneProject.entity.Question;
import com.son.CapstoneProject.entity.Report;
import com.son.CapstoneProject.entity.Tag;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.entity.uploadFile.UploadFileResponse;
import com.son.CapstoneProject.repository.ArticleRepository;
import com.son.CapstoneProject.repository.QuestionRepository;
import com.son.CapstoneProject.repository.ReportRepository;
import com.son.CapstoneProject.service.FileStorageService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = {"${front-end.settings.cross-origin.url}"})
public class AdminController {

    private Logger logger = Logger.getLogger(AdminController.class.getSimpleName());

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ControllerUtils controllerUtils;

    @GetMapping("/test")
    public String test() {
        return "You only see this if you are an administrator";
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
        String methodName = "AdminController.addArticle";

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
        String methodName = "AdminController.updateArticle";

        Article oldArticle = articleRepository.findById(articleId)
                .orElseThrow(() -> new Exception(methodName + ": Not found any article with id: " + articleId));

        // Save tags first
        List<Tag> tags = controllerUtils.saveDistinctiveTags(updatedArticle.getTags());

        // Update values
        oldArticle.setTitle(updatedArticle.getTitle());
        oldArticle.setContent(updatedArticle.getContent());
        oldArticle.setTags(tags);
        oldArticle.setFileDownloadUris(updatedArticle.getFileDownloadUris());
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
                .orElseThrow(() -> new Exception("AdminController.deleteArticle: Not found any article with id: " + id));

        // Delete article
        articleRepository.delete(article);

        Map<String, String> map = new HashMap<>();
        map.put("articleId", ("" + id));
        map.put("deleted", "true");
        return map;
    }

    @GetMapping("/viewAllReports")
    public List<Report> viewAllReports() {
        return reportRepository.findAll();
    }

    /**
     * ADMIN delete a question
     *
     * @param id
     * @return
     * @throws Exception
     */
    @DeleteMapping("/deleteQuestion/{id}")
    public Map<String, String> deleteQuestion(@PathVariable Long id) throws Exception {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new Exception("AdminController.deleteQuestion: Not found question with id: " + id));

        questionRepository.delete(question);
        Map<String, String> map = new HashMap<>();
        map.put("questionId", "" + id);
        map.put("deleted", "true");
        return map;
    }
}
