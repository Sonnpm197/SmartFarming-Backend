package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.Application;
import com.son.CapstoneProject.controller.CommonTest;
import com.son.CapstoneProject.entity.*;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.entity.pagination.QuestionPagination;
import com.son.CapstoneProject.repository.AppUserTagRepository;
import com.son.CapstoneProject.repository.QuestionRepository;
import com.son.CapstoneProject.repository.TagRepository;
import com.son.CapstoneProject.repository.UploadedFileRepository;
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
import java.util.List;
import java.util.Map;

import static com.son.CapstoneProject.controller.CommonTest.createURL;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
public class QuestionControllerTest {

    @LocalServerPort
    private int port;

    @Value("${front-end.settings.cross-origin.url}")
    private String frontEndUrl;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AppUserTagRepository appUserTagRepository;

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private TagRepository tagRepository;

    @Test
    @SqlGroup({
            @Sql("/sql/questionController/insert_question.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void viewNumberOfQuestions() {
        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("GET", frontEndUrl));
        ResponseEntity<String> response = CommonTest.getRestTemplate().exchange(
                createURL(port, "/question/viewNumberOfQuestions"),
                HttpMethod.GET,
                entity,
                String.class);
        String expected = "5"; // 5 questions
        System.out.println(">> Result: " + response.getBody());
        Assert.assertEquals(expected, response.getBody());
    }

    @Test
    @SqlGroup({
            @Sql("/sql/questionController/insert_question.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void viewNumberOfPages() {
        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("GET", frontEndUrl));
        ResponseEntity<String> response = CommonTest.getRestTemplate().exchange(
                createURL(port, "/question/viewNumberOfPages"),
                HttpMethod.GET,
                entity,
                String.class);
        String expected = "2"; // 5 questions, 3 questions per page => 2 pages
        System.out.println(">> Result: " + response.getBody());
        Assert.assertEquals(expected, response.getBody());
    }

    @Test
    @SqlGroup({
            @Sql("/sql/questionController/insert_question.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void viewQuestionsByPageIndex() {
        String url = createURL(port, "/question/viewQuestions/{pageNumber}");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("pageNumber", 0);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("GET", frontEndUrl));
        ResponseEntity<QuestionPagination> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<QuestionPagination>() {
                });

        QuestionPagination questionList = response.getBody();
        System.out.println(">> Result: " + questionList);
//        for (int i = 0; i < questionList.size(); i++) {
//            Question question = questionList.get(i);
//            // Assert if the higher article has higher date
//            // 5 > 4 > 3 > 2 > 1
//            if (i - 1 < 0) {
//                break;
//            }
//            Assert.assertTrue(question.getUtilTimestamp().compareTo(questionList.get(i - 1).getUtilTimestamp()) >= 0);
//        }
    }

    @Test
    @SqlGroup({
            @Sql("/sql/questionController/insert_question.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void viewQuestionById() {
        String url = createURL(port, "/question/viewQuestion/{id}");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("id", 1);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("GET", frontEndUrl));
        ResponseEntity<Question> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Question>() {
                });

        System.out.println(">> Result: " + response.getBody());

        // Increase view of question
        Question question = questionRepository.findById(1L).get();
        Assert.assertEquals(1, question.getViewCount());

        // Increase view of author
        AppUser appUser = appUserRepository.findById(1L).get();
        Assert.assertEquals(1, appUser.getViewCount());

        // Increase AppUserTag
        // Trong trot
        AppUserTag appUserTagTrongTrot = appUserTagRepository
                .findAppUserTagByAppUser_UserIdAndTag_TagId(appUser.getUserId(), 0L);
        // Chan nuoi
        AppUserTag appUserTagChanNuoi = appUserTagRepository
                .findAppUserTagByAppUser_UserIdAndTag_TagId(appUser.getUserId(), 1L);

        Assert.assertEquals(1, appUserTagTrongTrot.getViewCount());
        Assert.assertEquals(1, appUserTagChanNuoi.getViewCount());

        // Increase view of tags
        Tag trongTrot = tagRepository.findById(0L).get();
        Tag chanNuoi = tagRepository.findById(1L).get();
        Assert.assertEquals(1, trongTrot.getViewCount());
        Assert.assertEquals(1, chanNuoi.getViewCount());

    }

    /**
     * TODO: this method only test with indexed items
     */
    @Test
    public void searchQuestions() {

        String url = createURL(port, "/question/searchQuestions");

        String requestBody = "{"
                + "\"textSearch\" : " + "\"hà nội chán\""
                + "}";

        // URI (URL) parameters
        Map<String, String> uriParams = new HashMap<>();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("POST", frontEndUrl));
        ResponseEntity<List<Question>> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<List<Question>>() {
                });

        System.out.println(">> Result: " + response.getBody());

        // TODO: assert only with indexed items
        // Assert.assertEquals("người miền Nam sinh sống ở HN", response.getBody().get(0).getTitle());

    }

    @Test
    @SqlGroup({
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = BEFORE_TEST_METHOD),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void addQuestion() {

        String url = createURL(port, "/question/addQuestion");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\questionController\\addQuestion.json");

        // URI (URL) parameters
        Map<String, String> uriParams = new HashMap<>();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("POST", frontEndUrl));
        ResponseEntity<Question> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Question>() {
                });

        System.out.println(">> Result: " + response.getBody());

        // Check saved question
        Question question = response.getBody();
        Assert.assertEquals("Yêu đơn phương", question.getTitle());
        Assert.assertEquals("Năm nay mình 28 tuổi, đang làm cùng chỗ với crush.", question.getContent());

        // Check saved tags
        List<Tag> tags = question.getTags();
        Assert.assertEquals("trồng trọt description", tags.get(0).getDescription());
        Assert.assertEquals("chăn nuôi description", tags.get(1).getDescription());

        // Check saved user whether he is anonymous
        // AppUser is not represented in response but still saved in DB because JsonBackReference
        AppUser author = appUserRepository.findByIpAddress("127.0.0.1");
        Assert.assertTrue(author.isAnonymous());

        // Check UploadedFiles
        List<UploadedFile> uploadedFiles = uploadedFileRepository.findByQuestion_QuestionId(question.getQuestionId());
        Assert.assertEquals("trồng trọt", uploadedFiles.get(0).getUploadedFileUrlShownOnUI());
        Assert.assertEquals("trồng trọt", uploadedFiles.get(1).getUploadedFileUrlShownOnUI());

    }

    @Test
    @SqlGroup({
            @Sql("/sql/questionController/insert_question.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void updateQuestion() {

        String url = createURL(port, "/question/updateQuestion/{id}");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\questionController\\updateQuestion.json");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("id", 1);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("PUT", frontEndUrl));
        ResponseEntity<Question> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<Question>() {
                });

        System.out.println(">> Result: " + response.getBody());

        // Check saved question
        Question question = response.getBody();
        Assert.assertEquals("Yêu đơn phương", question.getTitle());
        Assert.assertEquals("Năm nay mình 28 tuổi, đang làm cùng chỗ với crush.", question.getContent());

        // Check saved tags
        List<Tag> tags = question.getTags();
        Assert.assertEquals("trồng trọt description", tags.get(0).getDescription());
        Assert.assertEquals("chăn nuôi description", tags.get(1).getDescription());

        // Check saved user whether he is anonymous
        // AppUser is not represented in response but still saved in DB because JsonBackReference
        AppUser author = appUserRepository.findByIpAddress("127.0.0.1");
        Assert.assertTrue(!author.isAnonymous());

        // Check UploadedFiles
        List<UploadedFile> uploadedFiles = uploadedFileRepository.findByQuestion_QuestionId(question.getQuestionId());
        Assert.assertEquals("trồng trọt", uploadedFiles.get(0).getUploadedFileUrlShownOnUI());
        Assert.assertEquals("trồng trọt", uploadedFiles.get(1).getUploadedFileUrlShownOnUI());

    }

    @Test
    @SqlGroup({
            @Sql("/sql/questionController/insert_question.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void deleteQuestion() {
    }

    @Test
    public void editOtherUserQuestion() {
    }

    @Test
    public void viewEditedVersions() {
    }

    @Test
    public void approveEditedVersion() {
    }

    @Test
    public void reportQuestion() {
    }
}