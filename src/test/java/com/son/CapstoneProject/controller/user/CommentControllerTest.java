package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.Application;
import com.son.CapstoneProject.controller.CommonTest;
import com.son.CapstoneProject.entity.Comment;
import com.son.CapstoneProject.entity.Question;
import com.son.CapstoneProject.entity.Tag;
import com.son.CapstoneProject.entity.UploadedFile;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.repository.CommentRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.son.CapstoneProject.controller.CommonTest.createURL;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
public class CommentControllerTest {

    @Autowired
    private CommentRepository commentRepository;

    @LocalServerPort
    private int port;

    @Value("${front-end.settings.cross-origin.url}")
    private String frontEndUrl;

    @Test
    @SqlGroup({
            @Sql(value = "/sql/articleController/insert_article.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void addCommentToArticle() {
        String url = createURL(port, "/comment/addComment");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\commentController\\addCommentToArticle.json");

        // URI (URL) parameters
        Map<String, String> uriParams = new HashMap<>();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("POST", frontEndUrl));
        ResponseEntity<Comment> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Comment>() {
                });

        System.out.println(">> Result: " + response.getBody());
        Assert.assertEquals("Bài viết khá hay", response.getBody().getContent());
    }

    @Test
    @SqlGroup({
            @Sql("/sql/questionController/insert_question.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void addCommentToQuestion() {
        String url = createURL(port, "/comment/addComment");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\commentController\\addCommentToQuestion.json");

        // URI (URL) parameters
        Map<String, String> uriParams = new HashMap<>();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("POST", frontEndUrl));
        ResponseEntity<Comment> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Comment>() {
                });

        System.out.println(">> Result: " + response.getBody());
        Assert.assertEquals("Bài viết khá hay", response.getBody().getContent());
    }

    @Test
    @SqlGroup({
            @Sql("/sql/answerController/insert_answer.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void addCommentToAnswer() {
        String url = createURL(port, "/comment/addComment");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\commentController\\addCommentToAnswer.json");

        // URI (URL) parameters
        Map<String, String> uriParams = new HashMap<>();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("POST", frontEndUrl));
        ResponseEntity<Comment> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Comment>() {
                });

        System.out.println(">> Result: " + response.getBody());
        Assert.assertEquals("Bài viết khá hay", response.getBody().getContent());
    }

    @Test
    @SqlGroup({
            @Sql("/sql/answerController/insert_answer.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void updateComment() {
        String url = createURL(port, "/comment/updateComment/{id}");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\commentController\\addCommentToAnswer.json");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("id", 1);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("PUT", frontEndUrl));
        ResponseEntity<Comment> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<Comment>() {
                });

        System.out.println(">> Result: " + response.getBody());
        Assert.assertEquals("Bài viết khá hay", response.getBody().getContent());
    }

    /**
     * An user with id 2 wants to edit comment of the user 1
     */
    @Test
    @SqlGroup({
            @Sql("/sql/answerController/insert_answer.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void updateOtherUserComment() {
        String url = createURL(port, "/comment/updateComment/{id}");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\commentController\\updateOtherUserComment.json");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("id", 1);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("PUT", frontEndUrl));
        try {
            ResponseEntity<Comment> response = CommonTest.getRestTemplate().exchange(
                    builder.buildAndExpand(uriParams).toUri(),
                    HttpMethod.PUT,
                    entity,
                    new ParameterizedTypeReference<Comment>() {
                    });

            System.out.println(">> Result: " + response.getBody());
            Assert.assertEquals("Bài viết khá hay", response.getBody().getContent());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertEquals("500 null", e.getMessage());
        }
    }

    @Test
    @SqlGroup({
            @Sql("/sql/answerController/insert_answer.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void deleteComment() {
        String url = createURL(port, "/comment/deleteComment/{id}");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\commentController\\deleteComment.json");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("id", 1);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("DELETE", frontEndUrl));
        ResponseEntity<Comment> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<Comment>() {
                });

        System.out.println(">> Result: " + response.getBody());
        Assert.assertEquals(0, commentRepository.count());
    }

    @Test
    @SqlGroup({
            @Sql("/sql/answerController/insert_answer.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void deleteOtherUserComment() {
        try {
            String url = createURL(port, "/comment/deleteComment/{id}");

            String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\commentController\\deleteOtherUserComment.json");

            // URI (URL) parameters
            Map<String, Integer> uriParams = new HashMap<>();
            uriParams.put("id", 1);

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

            System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

            HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("DELETE", frontEndUrl));
            ResponseEntity<Comment> response = CommonTest.getRestTemplate().exchange(
                    builder.buildAndExpand(uriParams).toUri(),
                    HttpMethod.DELETE,
                    entity,
                    new ParameterizedTypeReference<Comment>() {
                    });

            System.out.println(">> Result: " + response.getBody());
            Assert.assertEquals(0, commentRepository.count());
        } catch (Exception e) {
            Assert.assertEquals("500 null", e.getMessage());
        }
    }
}