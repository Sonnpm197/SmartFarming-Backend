package com.son.CapstoneProject.controller.admin;

import com.son.CapstoneProject.Application;
import com.son.CapstoneProject.controller.CommonTest;
import com.son.CapstoneProject.entity.Article;
import com.son.CapstoneProject.entity.RestResponsePage;
import com.son.CapstoneProject.entity.Tag;
import com.son.CapstoneProject.entity.pagination.ArticlePagination;
import com.son.CapstoneProject.repository.ArticleRepository;
import com.son.CapstoneProject.repository.TagRepository;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

//    String url = "http://test.com/solarSystem/planets/{planet}/moons/{moon}";
//
//    // URI (URL) parameters
//    Map<String, String> uriParams = new HashMap<String, String>();
//uriParams.put("planets", "Mars");
//uriParams.put("moons", "Phobos");
//
//    // Query parameters
//    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
//            // Add query parameter
//            .queryParam("firstName", "Mark")
//            .queryParam("lastName", "Watney");
//
//System.out.println(builder.buildAndExpand(uriParams).toUri());
///**
// * Console output:
// * http://test.com/solarSystem/planets/Mars/moons/Phobos?firstName=Mark&lastName=Watney
// */
//
//restTemplate.exchange(builder.buildAndExpand(uriParams).toUri() , HttpMethod.PUT,
//    requestEntity, class_p);

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
public class ArticleControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private TagRepository tagRepository;

    @Value("${front-end.settings.cross-origin.url}")
    private String frontEndUrl;

    private String createURL(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    @SqlGroup({
            @Sql("/sql/articleController/insert_article.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void viewNumberOfArticles() {
        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("GET", frontEndUrl));
        ResponseEntity<String> response = CommonTest.getRestTemplate().exchange(
                createURL("/article/viewNumberOfArticles"),
                HttpMethod.GET,
                entity,
                String.class);
        String expected = "8";
        System.out.println(">> Result: " + response.getBody());
        Assert.assertEquals(expected, response.getBody());
        // JSONAssert.assertEquals(expected, response.getBody(), false);
    }

    @Test
    @SqlGroup({
            @Sql("/sql/articleController/insert_article.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void viewArticlesByPageIndex() {
        String url = createURL("/article/viewArticles/{pageNumber}");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("pageNumber", 0);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("GET", frontEndUrl));
        ResponseEntity<ArticlePagination> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ArticlePagination>() {
                });

        List<Article> articleList = response.getBody().getArticlesByPageIndex();
        System.out.println(">> Result: " + articleList);
        for (int i = 0; i < articleList.size(); i++) {
            Article article = articleList.get(i);
            // Assert if the higher article has higher date
            // 5 > 4 > 3 > 2 > 1
            if (i - 1 < 0) {
                break;
            }
            Assert.assertTrue(article.getUtilTimestamp().compareTo(articleList.get(i - 1).getUtilTimestamp()) >= 0);
        }
    }

    @Test
    @SqlGroup({
            @Sql("/sql/articleController/insert_article.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void viewArticleById() {
        String url = createURL("/article/viewArticle/{id}");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("id", 1);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("GET", frontEndUrl));
        ResponseEntity<Article> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Article>() {
                });

        System.out.println(">> Result: " + response.getBody());

        // Assert view count of this article
        Article article = articleRepository.findById(1L).get();
        Assert.assertEquals(1, article.getViewCount());

        // Assert view count of tags
        // Article 1 has 2 tags id 0 & 1: trồng trọt & chăn nuôi
        Tag trongTrot = tagRepository.findById(0L).get();
        Tag chanNuoi = tagRepository.findById(1L).get();
        Assert.assertEquals(1, trongTrot.getViewCount());
        Assert.assertEquals(1, chanNuoi.getViewCount());
    }

    /**
     * Note*: this method only test with indexed items
     */
    @Test
    @SqlGroup({
            @Sql("/sql/articleController/insert_article.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void searchArticles() {
        String url = createURL("/article/searchArticles/0");

        String requestBody = "{"
                + "\"category\" : " + "\"trồng trọt\","
                + "\"textSearch\" : " + "\"hà nội chán\""
                + "}";

        // URI (URL) parameters
        Map<String, String> uriParams = new HashMap<>();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("POST", frontEndUrl));
        ResponseEntity<ArticlePagination> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ArticlePagination>() {
                });

        System.out.println(">> Result: " + response.getBody());
//        Assert.assertEquals("người miền Nam sinh sống ở HN", response.getBody().getArticlesByPageIndex().get(0).getTitle());
    }

    private Article loadArticle(String filePath) throws IOException {
        return new ObjectMapper().readValue(new File(filePath), Article.class);
    }
}