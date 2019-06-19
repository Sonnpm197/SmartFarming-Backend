package com.son.CapstoneProject.controller.authenticated;

import com.son.CapstoneProject.entity.Article;
import com.son.CapstoneProject.repository.ArticleRepository;
import com.son.CapstoneProject.uploadFile.FileStorageService;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(AdminController.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ArticleRepository articleRepository;

    @MockBean
    private FileStorageService fileStorageService;

    @Value("${front-end.settings.cross-origin.url}")
    private String frontEndUrl;

    @Test
    public void addArticle() {
        String filePath = "src\\test\\resources\\Article.json";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Article article = objectMapper.readValue(new File(filePath), Article.class);

            // Mock article whenever we call articleRepository.save()
            when(articleRepository.save(any(Article.class))).thenReturn(article);

            // Read json file to string
            String jsonBody = new String(Files.readAllBytes(Paths.get(filePath)));

            // Then perform call like the real situation
            MvcResult mvcResult = mvc.perform(post("/admin/addArticle")
                    .content(jsonBody)
                    .header("Access-Control-Request-Method", "POST") // Add this header for CORS requirement of angularjs
                    .header("Origin", frontEndUrl) // Add this header for CORS requirement of angularjs
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    // I did it on purpose. The returned title is actually : "Chanh dây bò vào vùng 'chảo lửa'"
                    .andExpect(jsonPath("$.title").value("Chanh dây bò vào vùng 'chảo lửaa'"))
                    .andReturn();

            System.out.println(mvcResult.getResponse().getContentAsString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void updateArticle() {
        // TODO: finish this
    }

    @Test
    public void deleteArticle() {
        String filePath = "src\\test\\resources\\Article.json";
        try {
            Article article = new ObjectMapper().readValue(new File(filePath), Article.class);

            given(articleRepository.findById(any(Long.class)))
                    .willAnswer(article1 -> { throw new Exception("Not found to delete"); });

            // Then perform call like the real situation
            MvcResult mvcResult = mvc.perform(delete("/admin/deleteArticle/{id}", 1)
                    .header("Access-Control-Request-Method", "DELETE")
                    .header("Origin", frontEndUrl)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();

            System.out.println(mvcResult.getResponse().getContentAsString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}