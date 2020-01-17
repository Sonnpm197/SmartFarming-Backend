package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.Application;
import com.son.CapstoneProject.controller.CommonTest;
import com.son.CapstoneProject.common.entity.Answer;
import com.son.CapstoneProject.common.entity.AppUserTag;
import com.son.CapstoneProject.common.entity.Comment;
import com.son.CapstoneProject.common.entity.login.AppUser;
import com.son.CapstoneProject.repository.AnswerRepository;
import com.son.CapstoneProject.repository.AppUserTagRepository;
import com.son.CapstoneProject.repository.CommentRepository;
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

import static com.son.CapstoneProject.controller.CommonTest.createURL;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
public class AnswerControllerTest {

    @LocalServerPort
    private int port;

    @Value("${front-end.settings.cross-origin.url}")
    private String frontEndUrl;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private AppUserTagRepository appUserTagRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    @SqlGroup({
            @Sql(value = "/sql/questionController/insert_question.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void addAnswerToQuestion() {
        String url = createURL(port, "/answer/addAnswerToQuestion");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\answerController\\addAnswerToQuestion.json");

        // URI (URL) parameters
        Map<String, String> uriParams = new HashMap<>();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("POST", frontEndUrl));
        ResponseEntity<Answer> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Answer>() {
                });

        System.out.println(">> Result: " + response.getBody());
        Assert.assertEquals("Câu hỏi khá hay", response.getBody().getContent());
    }

    @Test
    @SqlGroup({
            @Sql("/sql/questionController/insert_question.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void updateAnswerToQuestion() {
        String url = createURL(port, "/answer/updateAnswerToQuestion/{answerId}");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\answerController\\addAnswerToQuestion.json");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("answerId", 1);
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
        Assert.assertEquals("Câu hỏi khá hay", response.getBody().getContent());
    }

    @Test
    @SqlGroup({
            @Sql("/sql/questionController/insert_question.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void updateOtherUserAnswerToQuestion() {
        String url = createURL(port, "/answer/updateAnswerToQuestion/{answerId}");

        // Update from user 2 to the answer but the owner is user with id = 1
        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\answerController\\updateOtherUserAnswer.json");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("answerId", 1);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        try {
            HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("PUT", frontEndUrl));
            ResponseEntity<Comment> response = CommonTest.getRestTemplate().exchange(
                    builder.buildAndExpand(uriParams).toUri(),
                    HttpMethod.PUT,
                    entity,
                    new ParameterizedTypeReference<Comment>() {
                    });

            System.out.println(">> Result: " + response.getBody());
            Assert.assertEquals("Câu hỏi khá hay", response.getBody().getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Also test delete comment
     */
    @Test
    @SqlGroup({
            @Sql("/sql/questionController/insert_question.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void deleteYourAnswerToQuestion() {
        String url = createURL(port, "/answer/deleteAnswerToQuestion/{answerId}");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\answerController\\deleteAnswer.json");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("answerId", 1);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("DELETE", frontEndUrl));
        ResponseEntity<String> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<String>() {
                });

        System.out.println(">> Result: " + response.getBody());
        Assert.assertFalse(answerRepository.findById(1L).isPresent());
    }

    /**
     * The author can mark accepted answer
     * => the user who answer increase reputation
     * <p>
     * 2 answer => 1 (author) can mark as accepted ans => count reputation for 2
     */
    @Test
    @SqlGroup({
            @Sql("/sql/questionController/insert_question.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void markAcceptedAnswerToQuestion() {
        String url = createURL(port, "/answer/markAcceptedAnswerToQuestion/{questionId}/{answerId}");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\answerController\\markAcceptedAnswer.json");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("questionId", 1);
        uriParams.put("answerId", 1); // cannot mark his own answer

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        try {
            HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("PUT", frontEndUrl));
            ResponseEntity<Answer> response = CommonTest.getRestTemplate().exchange(
                    builder.buildAndExpand(uriParams).toUri(),
                    HttpMethod.PUT,
                    entity,
                    new ParameterizedTypeReference<Answer>() {
                    });

            System.out.println(">> Result: " + response.getBody());
            Assert.assertEquals("Bài viết khá hay", response.getBody().getContent());
        } catch (Exception e) {
            e.printStackTrace(); // An exception is thrown here
        }
    }

    @Test
    @SqlGroup({
            @Sql("/sql/questionController/insert_question.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void markAcceptedAnswerOfOtherUserToQuestion() {
        String url = createURL(port, "/answer/markAcceptedAnswerToQuestion/{questionId}/{answerId}");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\answerController\\markAcceptedAnswer.json");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("questionId", 1);
        uriParams.put("answerId", 2); // mark other user answer

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("PUT", frontEndUrl));
        ResponseEntity<Answer> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<Answer>() {
                });

        System.out.println(">> Result: " + response.getBody());
        Assert.assertEquals("Câu trả lời đó là của người số 2 cho câu hỏi 1", response.getBody().getContent());

        // Check reputation of user answers question (2nd user)
        AppUser appUser = appUserRepository.findById(2L).get();
        Assert.assertEquals(1, appUser.getReputation());

        // Check AppUserTag point of this user
        // This question has 2 tag 'trồng trọt' & 'chăn nuôi' => validate this
        AppUserTag user2TagTrongTrot = appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(2L, 0L);
        AppUserTag user2TagChanNuoi = appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(2L, 1L);
        Assert.assertEquals(1, user2TagTrongTrot.getReputation());
        Assert.assertEquals(1, user2TagChanNuoi.getReputation());

    }

    @Test
    @SqlGroup({
            @Sql("/sql/answerController/insert_question_with_accepted_answer.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void unmarkAcceptedAnswerToQuestion() {
        String url = createURL(port, "/answer/unmarkAcceptedAnswerToQuestion/{questionId}/{answerId}");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\answerController\\markAcceptedAnswer.json");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("questionId", 1);
        uriParams.put("answerId", 2); // unmark mark this answer

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("PUT", frontEndUrl));
        ResponseEntity<Answer> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<Answer>() {
                });

        System.out.println(">> Result: " + response.getBody());
        Assert.assertEquals("Câu trả lời đó là của người số 2 cho câu hỏi 1", response.getBody().getContent());

        // Check AppUserTag point of this user
        // This question has 2 tag 'trồng trọt' & 'chăn nuôi' => validate this
        AppUserTag user2TagTrongTrot = appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(2L, 0L);
        AppUserTag user2TagChanNuoi = appUserTagRepository.findAppUserTagByAppUser_UserIdAndTag_TagId(2L, 1L);
        Assert.assertEquals(0, user2TagTrongTrot.getReputation());
        Assert.assertEquals(0, user2TagChanNuoi.getReputation());

    }
}