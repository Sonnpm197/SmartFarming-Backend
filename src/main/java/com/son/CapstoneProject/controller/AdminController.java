package com.son.CapstoneProject.controller;

import com.son.CapstoneProject.entity.Article;
import com.son.CapstoneProject.entity.Question;
import com.son.CapstoneProject.entity.Report;
import com.son.CapstoneProject.entity.Tag;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.repository.ArticleRepository;
import com.son.CapstoneProject.repository.QuestionRepository;
import com.son.CapstoneProject.repository.ReportRepository;
import com.son.CapstoneProject.repository.TagRepository;
import com.son.CapstoneProject.repository.loginRepository.AppUserDAO;
import com.son.CapstoneProject.service.FileStorageService;
import com.son.CapstoneProject.entity.uploadFile.UploadFileResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.apache.log4j.Logger;

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
    private FileStorageService fileStorageService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private AppUserDAO appUserDAO;

    @Autowired
    private QuestionRepository questionRepository;

    @GetMapping("/test")
    public String test() {
        return "You only see this if you are an administrator";
    }

    @PostMapping("/uploadFile")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());
    }

    @PostMapping("/uploadMultipleFiles")
    public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        return Arrays.asList(files)
                .stream()
                .map(file -> uploadFile(file))
                .collect(Collectors.toList());
    }

    /**
     * This method is to download a file from URI
     * *Note: to use regular expression we need a format like: varName:regex
     *
     * @param fileName
     * @param request
     * @return
     */
    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
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
        AppUser appUser = article.getAppUser();

        if (appUser == null) {
            String message = "AdminController.addArticle: Request body has no appUser";
            logger.info(message);
            throw new Exception(message);
        }

        Long userId = appUser.getUserId();

        if (userId == null) {
            String message = "AdminController.addArticle: AppUser from request body has no user id";
            logger.info(message);
            throw new Exception(message);
        }

        article.setUtilTimestamp(new Date());

        // Save tags first (distinct name)
        List<Tag> tags = saveArticleTags(article);
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
        Article oldArticle = articleRepository.findById(articleId)
                .orElseThrow(() -> new Exception("AdminController.updateArticle: Not found any article with id: " + articleId));

        AppUser appUser = updatedArticle.getAppUser();

        if (appUser == null) {
            String message = "AdminController.updateArticle: Request body has no appUser";
            logger.info(message);
            throw new Exception(message);
        }

        Long userId = appUser.getUserId();

        if (userId == null) {
            String message = "AdminController.updateArticle: AppUser from request body has no user id";
            logger.info(message);
            throw new Exception(message);
        }

        // Save tags first
        List<Tag> tags = saveArticleTags(updatedArticle);

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

    /**
     * Only save tags which don't exist in the DB
     * @param article
     * @return
     */
    private List<Tag> saveArticleTags(Article article) {
        List<Tag> tags = article.getTags();
        List<Tag> processedList = new ArrayList<>();
        if (tags != null) {
            for (Tag tag : tags) {
                tag.setName(tag.getName().toLowerCase().trim());
                tag.setDescription(tag.getDescription().toLowerCase().trim());

                // Do not save if that tag existed
                if (tagRepository.findByName(tag.getName()).size() > 0) {
                    continue;
                }

                processedList.add(tag);
                tagRepository.save(tag);
            }
        }

        return processedList;
    }
}
