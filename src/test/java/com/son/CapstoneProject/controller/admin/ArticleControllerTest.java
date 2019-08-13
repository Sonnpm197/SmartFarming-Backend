package com.son.CapstoneProject.controller.admin;

import com.son.CapstoneProject.Application;
import com.son.CapstoneProject.controller.CommonTest;
import com.son.CapstoneProject.entity.Article;
import com.son.CapstoneProject.entity.RestResponsePage;
import com.son.CapstoneProject.entity.Tag;
import com.son.CapstoneProject.entity.pagination.ArticlePagination;
import com.son.CapstoneProject.repository.ArticleRepository;
import com.son.CapstoneProject.repository.CommentRepository;
import com.son.CapstoneProject.repository.TagRepository;
import com.son.CapstoneProject.repository.UploadedFileRepository;
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
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
public class ArticleControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Value("${front-end.settings.cross-origin.url}")
    private String frontEndUrl;

    private String createURL(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    @SqlGroup({
            @Sql("/sql/articleController/insert_article_multiple_categories.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void viewDistinctCategories() {
        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("GET", frontEndUrl));
        ResponseEntity<List<String>> response = CommonTest.getRestTemplate().exchange(
                createURL("/article/viewDistinctCategories"),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<String>>() {
                });
        System.out.println(">> Result: " + response.getBody());
        List<String> categories = response.getBody();
        Assert.assertEquals(3, categories.size());
        Assert.assertTrue(categories.contains("trồng trọt"));
        Assert.assertTrue(categories.contains("chăn nuôi"));
        Assert.assertTrue(categories.contains("kỹ thuật trồng"));
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
        String url = createURL("/article/viewArticles/{type}/{pageNumber}");

        // URI (URL) parameters
        Map<String, String> uriParams = new HashMap<>();
        uriParams.put("pageNumber", "0");
        uriParams.put("type", "date");

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
    public void viewArticlesByCategory() {
        String url = createURL("/article/viewArticlesByCategory/date/0");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\articleController\\searchArticle.json");

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
        System.out.println();
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
        String url = createURL("/article/searchArticles/{type}/0");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\articleController\\searchArticle.json");

        // URI (URL) parameters
        Map<String, String> uriParams = new HashMap<>();
        uriParams.put("type", "date");

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

    @Test
    @SqlGroup({
            @Sql("/sql/articleController/insert_article.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void addArticle() {
        String url = createURL("/article/addArticle");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\articleController\\addArticle.json");

        // URI (URL) parameters
        Map<String, String> uriParams = new HashMap<>();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        // Add query parameter
//            .queryParam("firstName", "Mark")
//            .queryParam("lastName", "Watney");

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("POST", frontEndUrl));
        ResponseEntity<Article> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Article>() {
                });

        Article article = response.getBody();
        System.out.println(">> Result: " + article);

        // Assert tags
        Assert.assertNotNull(tagRepository.findByName("sen sen"));
        Assert.assertNotNull(tagRepository.findByName("sen bị cá ăn"));

        // Assert uploaded file in db
        Assert.assertNotNull(uploadedFileRepository.findByBucketNameAndUploadedFileName("bucket_name", "uploaded_file_name_2"));
        Assert.assertNotNull(uploadedFileRepository.findByBucketNameAndUploadedFileName("bucket_name", "uploaded_file_name"));
    }

    @Test
    @SqlGroup({
            @Sql("/sql/articleController/insert_article.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void updateArticle() {
        String url = createURL("/article/updateArticle/{articleId}");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\articleController\\updateArticle.json");

        // URI (URL) parameters
        Map<String, Long> uriParams = new HashMap<>();
        uriParams.put("articleId", 1L);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        // Add query parameter
//            .queryParam("firstName", "Mark")
//            .queryParam("lastName", "Watney");

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("PUT", frontEndUrl));
        ResponseEntity<Article> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<Article>() {
                });

        Article article = response.getBody();
        System.out.println(">> Result: " + article);

        // Assert tags
        Assert.assertNotNull(tagRepository.findByName("sen sen"));
        Assert.assertNotNull(tagRepository.findByName("sen bị cá ăn"));

        // Assert uploaded file in db
        Assert.assertEquals(2, uploadedFileRepository.count());
        Assert.assertNotNull(uploadedFileRepository.findByBucketNameAndUploadedFileName("bucket_name", "updated_uploaded_file_name"));
        Assert.assertNotNull(uploadedFileRepository.findByBucketNameAndUploadedFileName("bucket_name", "updated_uploaded_file_name_2"));
    }

    @Test
    @SqlGroup({
            @Sql("/sql/articleController/insert_article.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void deleteArticle() {
        String url = createURL("/article/deleteArticle/{articleId}");

        // URI (URL) parameters
        Map<String, Long> uriParams = new HashMap<>();
        uriParams.put("articleId", 1L);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        // Add query parameter
//            .queryParam("firstName", "Mark")
//            .queryParam("lastName", "Watney");

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("DELETE", frontEndUrl));
        ResponseEntity<String> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<String>() {
                });

        System.out.println(">> Result: " + response.getBody());

        // Assert article
        Assert.assertFalse(articleRepository.findById(1L).isPresent());

        // Assert comments
        Assert.assertEquals(0, commentRepository.findByArticle_ArticleId(1L).size());

        // Assert updated file
        Assert.assertEquals(0, uploadedFileRepository.findByArticle_ArticleId(1L).size());
    }

    @Test
    @SqlGroup({
            @Sql("/sql/articleController/insert_article_top_10_by_upvote_count.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void getTop10ArticlesByUpvoteCount() {
        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("GET", frontEndUrl));
        ResponseEntity<ArticlePagination> response = CommonTest.getRestTemplate().exchange(
                createURL("/article/getTop10ArticlesByUpvoteCount"),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ArticlePagination>() {
                });
        int expected = 10;
        System.out.println(">> Result: " + response.getBody());
        Assert.assertTrue(expected == response.getBody().getArticlesByPageIndex().size());
    }

    @Test
    @SqlGroup({
            @Sql("/sql/articleController/insert_article_top_10_by_upload_date.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void getTop10ArticlesByUploadDate() {
        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("GET", frontEndUrl));
        ResponseEntity<ArticlePagination> response = CommonTest.getRestTemplate().exchange(
                createURL("/article/getTop10ArticlesByUploadDate"),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ArticlePagination>() {
                });
        int expected = 10;
        System.out.println(">> Result: " + response.getBody());
        Assert.assertTrue(expected == response.getBody().getArticlesByPageIndex().size());
    }

    @Test
    @SqlGroup({
            @Sql("/sql/articleController/find_articles_by_tags.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void findArticlesByTag() {
        String url = createURL("/article/viewArticlesByTag/date/0/0");

        // URI (URL) parameters
        Map<String, String> uriParams = new HashMap<>();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("GET", frontEndUrl));
        ResponseEntity<ArticlePagination> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ArticlePagination>() {
                });

        System.out.println(">> Result: " + response.getBody());
        Assert.assertTrue(response.getBody().getArticlesByPageIndex().size() == 1);
    }

    @Test
    @SqlGroup({
            @Sql("/sql/articleController/find_related_article.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void viewRelatedArticles() {
        String url = createURL("/article/viewRelatedArticles/1");

        // URI (URL) parameters
        Map<String, String> uriParams = new HashMap<>();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("GET", frontEndUrl));
        ResponseEntity<ArticlePagination> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ArticlePagination>() {
                });

        System.out.println(">> Result: " + response.getBody());
        Assert.assertTrue(response.getBody().getArticlesByPageIndex().size() == 1);
    }
}