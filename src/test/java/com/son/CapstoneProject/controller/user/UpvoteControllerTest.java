package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.Application;
import com.son.CapstoneProject.controller.CommonTest;
import com.son.CapstoneProject.repository.AppUserTagRepository;
import com.son.CapstoneProject.repository.TagRepository;
import com.son.CapstoneProject.repository.loginRepository.AppUserRepository;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

import static com.son.CapstoneProject.common.ConstantValue.*;
import static com.son.CapstoneProject.controller.CommonTest.createURL;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
public class UpvoteControllerTest {

    @LocalServerPort
    private int port;

    @Value("${front-end.settings.cross-origin.url}")
    private String frontEndUrl;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AppUserTagRepository appUserTagRepository;

    private void upvote(String type, String id, String filePath) {
        String url = createURL(port, "/upvote/{type}/{id}");

        String requestBody = CommonTest.readStringFromFile(filePath);

        // URI (URL) parameters
        Map<String, String> uriParams = new HashMap<>();
        uriParams.put("type", type);
        uriParams.put("id", id);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("POST", frontEndUrl));
        ResponseEntity<String> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<String>() {
                });

        System.out.println(">> Result: " + response.getBody());
    }


    /**
     * User id = 2 upvote question id = 1
     */
    @Test
    @SqlGroup({
            @Sql("/sql/upvoteController/insert.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void upvoteQuestionById() {

        upvote(QUESTION, "1", "src\\test\\resources\\json\\upvoteController\\upvote.json");

        // Check tags
        Assert.assertEquals(1, tagRepository.findById(0L).get().getReputation());
        Assert.assertEquals(1, tagRepository.findById(1L).get().getReputation());

        // Check author of that question
        Assert.assertEquals(1, appUserRepository.findById(1L).get().getReputation());
        // Check AppUserTag
        Assert.assertEquals(1, appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(1L, 0L).getReputation());
        Assert.assertEquals(1, appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(1L, 1L).getReputation());

    }

    /**
     * User id = 2 upvote question id = 1 2 times
     */
    @Test
    @SqlGroup({
            @Sql("/sql/upvoteController/insert.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void upvoteQuestion2Times() {

        upvote(QUESTION, "1", "src\\test\\resources\\json\\upvoteController\\upvote.json");
        upvote(QUESTION, "1", "src\\test\\resources\\json\\upvoteController\\upvote.json");

        // Check tags
        Assert.assertEquals(0, tagRepository.findById(0L).get().getReputation());
        Assert.assertEquals(0, tagRepository.findById(1L).get().getReputation());

        // Check author of that question
        Assert.assertEquals(0, appUserRepository.findById(1L).get().getReputation());
        // Check AppUserTag
        Assert.assertEquals(0, appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(1L, 0L).getReputation());
        Assert.assertEquals(0, appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(1L, 1L).getReputation());

    }

    /**
     * User id = 2 upvote article id = 1
     */
    @Test
    @SqlGroup({
            @Sql("/sql/upvoteController/insert.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void upvoteArticleById() {

        upvote(ARTICLE, "1", "src\\test\\resources\\json\\upvoteController\\upvote.json");

        // Check author of that question
        Assert.assertEquals(0, appUserRepository.findById(3L).get().getReputation());

        // Article posted by admin so he wont get points

        // Check AppUserTag
        Assert.assertNull(appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(3L, 0L));
        Assert.assertNull(appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(3L, 1L));

        // Check tags
        Assert.assertEquals(1, tagRepository.findById(0L).get().getReputation());
        Assert.assertEquals(1, tagRepository.findById(1L).get().getReputation());
    }

    /**
     * User id = 2 upvote article id = 1 2 times
     */
    @Test
    @SqlGroup({
            @Sql("/sql/upvoteController/insert.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void upvoteArticle2Times() {

        upvote(ARTICLE, "1", "src\\test\\resources\\json\\upvoteController\\upvote.json");
        upvote(ARTICLE, "1", "src\\test\\resources\\json\\upvoteController\\upvote.json");

        // Check author of that question
        Assert.assertEquals(0, appUserRepository.findById(3L).get().getReputation());
        // Check AppUserTag
        Assert.assertNull(appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(3L, 0L));
        Assert.assertNull(appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(3L, 1L));

        // Check tags
        Assert.assertEquals(0, tagRepository.findById(0L).get().getReputation());
        Assert.assertEquals(0, tagRepository.findById(1L).get().getReputation());

    }

    /**
     * User1 (author of this question1) upvote the answer of user2
     */
    @Test
    @SqlGroup({
            @Sql("/sql/upvoteController/insert.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void upvoteAnswerById() {

        upvote(ANSWER, "2", "src\\test\\resources\\json\\upvoteController\\upvoteAnswer.json");

        // Check author of that question
        Assert.assertEquals(1, appUserRepository.findById(2L).get().getReputation());
        // Check AppUserTag
        Assert.assertEquals(1, appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(2L, 0L).getReputation());
        Assert.assertEquals(1, appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(2L, 1L).getReputation());

        // Check tags
        Assert.assertEquals(1, tagRepository.findById(0L).get().getReputation());
        Assert.assertEquals(1, tagRepository.findById(1L).get().getReputation());

    }

    /**
     * User1 (author of this question1) upvote the answer of user2 2 times
     */
    @Test
    @SqlGroup({
            @Sql("/sql/upvoteController/insert.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void upvoteAnswer2Times() {

        upvote(ANSWER, "2", "src\\test\\resources\\json\\upvoteController\\upvoteAnswer.json");
        upvote(ANSWER, "2", "src\\test\\resources\\json\\upvoteController\\upvoteAnswer.json");

        // Check author of that answer
        Assert.assertEquals(0, appUserRepository.findById(2L).get().getReputation());

        // Check AppUserTag
        Assert.assertEquals(0, appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(2L, 0L).getReputation());
        Assert.assertEquals(0, appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(2L, 1L).getReputation());

        // Check tags
        Assert.assertEquals(0, tagRepository.findById(0L).get().getReputation());
        Assert.assertEquals(0, tagRepository.findById(1L).get().getReputation());

    }

    /**
     * User 2 upvote comment id = 16 of user1 for article ID = 1
     */
    @Test
    @SqlGroup({
            @Sql("/sql/upvoteController/insert.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void upvoteCommentForArticle() {

        upvote(COMMENT, "16", "src\\test\\resources\\json\\upvoteController\\upvoteCommentForArticle.json");

        // Check author of that question
        Assert.assertEquals(1, appUserRepository.findById(1L).get().getReputation());
        // Check AppUserTag
        Assert.assertEquals(1, appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(1L, 0L).getReputation());
        Assert.assertEquals(1, appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(1L, 1L).getReputation());

        // Check tags
        Assert.assertEquals(1, tagRepository.findById(0L).get().getReputation());
        Assert.assertEquals(1, tagRepository.findById(1L).get().getReputation());

    }

    /**
     * User 2 upvote comment id = 16 of user1 for article ID = 1 2 times
     */
    @Test
    @SqlGroup({
            @Sql("/sql/upvoteController/insert.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void upvoteCommentForArticle2Times() {

        upvote(COMMENT, "16", "src\\test\\resources\\json\\upvoteController\\upvoteCommentForArticle.json");
        upvote(COMMENT, "16", "src\\test\\resources\\json\\upvoteController\\upvoteCommentForArticle.json");

        // Check author of that question
        Assert.assertEquals(0, appUserRepository.findById(1L).get().getReputation());
        // Check AppUserTag
        Assert.assertEquals(0, appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(1L, 0L).getReputation());
        Assert.assertEquals(0, appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(1L, 1L).getReputation());

        // Check tags
        Assert.assertEquals(0, tagRepository.findById(0L).get().getReputation());
        Assert.assertEquals(0, tagRepository.findById(1L).get().getReputation());

    }

    /**
     * User 2 upvote for comment id = 13 of user 1 of question 1
     */
    @Test
    @SqlGroup({
            @Sql("/sql/upvoteController/insert.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void upvoteCommentForQuestion() {

        upvote(COMMENT, "13", "src\\test\\resources\\json\\upvoteController\\upvoteCommentForQuestion.json");

        // Check author of that question
        Assert.assertEquals(1, appUserRepository.findById(1L).get().getReputation());
        // Check AppUserTag
        Assert.assertEquals(1, appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(1L, 0L).getReputation());
        Assert.assertEquals(1, appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(1L, 1L).getReputation());

        // Check tags
        Assert.assertEquals(1, tagRepository.findById(0L).get().getReputation());
        Assert.assertEquals(1, tagRepository.findById(1L).get().getReputation());

    }

    /**
     * User 2 upvote for comment id = 13 of user 1 of question 1 2 times
     */
    @Test
    @SqlGroup({
            @Sql("/sql/upvoteController/insert.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void upvoteCommentForQuestion2Times() {

        upvote(COMMENT, "13", "src\\test\\resources\\json\\upvoteController\\upvoteCommentForQuestion.json");
        upvote(COMMENT, "13", "src\\test\\resources\\json\\upvoteController\\upvoteCommentForQuestion.json");

        // Check author of that question
        Assert.assertEquals(0, appUserRepository.findById(1L).get().getReputation());
        // Check AppUserTag
        Assert.assertEquals(0, appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(1L, 0L).getReputation());
        Assert.assertEquals(0, appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(1L, 1L).getReputation());

        // Check tags
        Assert.assertEquals(0, tagRepository.findById(0L).get().getReputation());
        Assert.assertEquals(0, tagRepository.findById(1L).get().getReputation());

    }

    /**
     * User 2 upvotes comment of answer id 1 of question 1
     */
    @Test
    @SqlGroup({
            @Sql("/sql/upvoteController/insert.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void upvoteCommentForAnswer() {

        upvote(COMMENT, "10", "src\\test\\resources\\json\\upvoteController\\upvoteCommentForQuestion.json");

        // Check author of that question
        Assert.assertEquals(1, appUserRepository.findById(1L).get().getReputation());
        // Check AppUserTag
        Assert.assertEquals(1, appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(1L, 0L).getReputation());
        Assert.assertEquals(1, appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(1L, 1L).getReputation());

        // Check tags
        Assert.assertEquals(1, tagRepository.findById(0L).get().getReputation());
        Assert.assertEquals(1, tagRepository.findById(1L).get().getReputation());

    }

    /**
     * User 2 upvotes comment of answer id 1 of question 1
     */
    @Test
    @SqlGroup({
            @Sql("/sql/upvoteController/insert.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void upvoteCommentForAnswer2Times() {

        upvote(COMMENT, "10", "src\\test\\resources\\json\\upvoteController\\upvoteCommentForQuestion.json");
        upvote(COMMENT, "10", "src\\test\\resources\\json\\upvoteController\\upvoteCommentForQuestion.json");

        // Check author of that question
        Assert.assertEquals(0, appUserRepository.findById(1L).get().getReputation());
        // Check AppUserTag
        Assert.assertEquals(0, appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(1L, 0L).getReputation());
        Assert.assertEquals(0, appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(1L, 1L).getReputation());

        // Check tags
        Assert.assertEquals(0, tagRepository.findById(0L).get().getReputation());
        Assert.assertEquals(0, tagRepository.findById(1L).get().getReputation());

    }
}