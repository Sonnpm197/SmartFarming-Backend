package com.son.CapstoneProject.controller.authenticated;

import com.son.CapstoneProject.domain.Article;
import com.son.CapstoneProject.repository.ArticleRepository;
import com.son.CapstoneProject.uploadFile.FileStorageService;
import com.son.CapstoneProject.uploadFile.UploadFileResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ArticleRepository articleRepository;

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
     *
     * @param article
     * @return
     */
    @PostMapping(value = "/addArticle",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Article addArticle(@RequestBody Article article) {
        return articleRepository.save(article);
    }

    /**
     * Admins can update an article
     *
     * @param updatedArticle
     * @param id
     * @return
     * @throws Exception
     */
    @PutMapping("/updateArticle/{id}")
    public ResponseEntity<Article> updateArticle(
            @RequestBody Article updatedArticle,
            @PathVariable Long id)
            throws Exception {
        Article oldSkill = articleRepository.findById(id)
                .orElseThrow(() -> new Exception("Not found"));

        // Update values
        oldSkill.setTitle(updatedArticle.getTitle());
        oldSkill.setContent(updatedArticle.getContent());

        Article question = articleRepository.save(oldSkill);
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
    public Map<String, Boolean> deleteArticle(@PathVariable Long id) throws Exception {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new Exception("Not found to delete"));

        // Delete article
        articleRepository.delete(article);

        Map<String, Boolean> map = new HashMap<>();
        map.put("deleteArticle: " + id, Boolean.TRUE);
        return map;
    }
}
