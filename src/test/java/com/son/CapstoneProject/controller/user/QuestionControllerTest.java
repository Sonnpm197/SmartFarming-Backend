package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.Application;
import com.son.CapstoneProject.controller.CommonTest;
import com.son.CapstoneProject.entity.*;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.entity.pagination.Pagination;
import com.son.CapstoneProject.entity.pagination.QuestionPagination;
import com.son.CapstoneProject.repository.*;
import com.son.CapstoneProject.repository.loginRepository.AppUserRepository;
import io.opencensus.tags.Tags;
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
    private EditedQuestionRepository editedQuestionRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AppUserTagRepository appUserTagRepository;

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private ReportRepository reportRepository;

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
    public void viewTop3QuestionsByViewCount() {
        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("GET", frontEndUrl));
        ResponseEntity<List<Question>> response = CommonTest.getRestTemplate().exchange(
                createURL(port, "/question/viewTop3QuestionsByViewCount"),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<Question>>() {
                });
        System.out.println(">> Result: " + response.getBody());
//        Assert.assertEquals(expected, response.getBody());
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

    /**
     * Comment async run to pass this or press run directly
     */
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

        String url = createURL(port, "/question/searchQuestions/0");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\questionController\\searchQuestion.json");

        // URI (URL) parameters
        Map<String, String> uriParams = new HashMap<>();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("POST", frontEndUrl));
        ResponseEntity<QuestionPagination> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<QuestionPagination>() {
                });

        QuestionPagination pagination = response.getBody();
        System.out.println(">> Result: " + pagination);

        // TODO: assert only with indexed items
//         Assert.assertEquals("người miền Nam sinh sống ở HN", response.getBody().getQa().get(0).getTitle());

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
        // We already had "trong trot" & "chan nuoi" tags in DB, so json body would not affect
        List<Tag> tags = question.getTags();
        Assert.assertEquals("tagDescription 0", tags.get(0).getDescription());
        Assert.assertEquals("tagDescription 1", tags.get(1).getDescription());

        // Check saved user whether he is anonymous
        // AppUser is not represented in response but still saved in DB because JsonBackReference
        AppUser author = appUserRepository.findByIpAddress("127.0.0.1");
        Assert.assertTrue(!author.isAnonymous());

        // Check UploadedFiles
        List<UploadedFile> uploadedFiles = uploadedFileRepository.findByQuestion_QuestionId(question.getQuestionId());
        Assert.assertEquals(2, uploadedFiles.size()); // To test deleting old files on GG cloud and on DB
        Assert.assertEquals("uploaded_file_url_shown_onui", uploadedFiles.get(0).getUploadedFileUrlShownOnUI());
        Assert.assertEquals("uploaded_file_url_shown_onui_2", uploadedFiles.get(1).getUploadedFileUrlShownOnUI());

    }

    @Test
    @SqlGroup({
            @Sql("/sql/questionController/insert_question.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void deleteQuestion() {

        String url = createURL(port, "/question/deleteQuestion/{id}");

        //String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\questionController\\deleteQuestion.json");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("id", 1);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

//        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("DELETE", frontEndUrl));
        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("DELETE", frontEndUrl));
        ResponseEntity<Question> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<Question>() {
                });

        System.out.println(">> Result: " + response.getBody());

        // Assert comment of this question

        Assert.assertFalse(commentRepository.findById(13L).isPresent());
        Assert.assertFalse(commentRepository.findById(14L).isPresent());
        Assert.assertFalse(commentRepository.findById(15L).isPresent());

        // Assert answers of this question
        Assert.assertFalse(answerRepository.findById(1L).isPresent());
        Assert.assertFalse(answerRepository.findById(2L).isPresent());

        // Assert comment of answer1
        Assert.assertFalse(commentRepository.findById(10L).isPresent());
        Assert.assertFalse(commentRepository.findById(11L).isPresent());
        Assert.assertFalse(commentRepository.findById(12L).isPresent());

        // Assert uploadedFile
        List<UploadedFile> uploadedFiles = uploadedFileRepository.findByQuestion_QuestionId(1L);

        Assert.assertEquals(0, uploadedFiles.size());

        // Assert report
        Assert.assertFalse(reportRepository.findById(1L).isPresent());
        Assert.assertFalse(reportRepository.findById(2L).isPresent());

        // Assert editedQuestion
        Assert.assertFalse(editedQuestionRepository.findById(1L).isPresent());
        Assert.assertFalse(editedQuestionRepository.findById(2L).isPresent());

        // Tags must be kept
        Assert.assertTrue(tagRepository.findById(0L).isPresent());
        Assert.assertTrue(tagRepository.findById(1L).isPresent());
        Assert.assertTrue(tagRepository.findById(2L).isPresent());
        Assert.assertTrue(tagRepository.findById(3L).isPresent());
    }

    @Test
    @SqlGroup({
            @Sql("/sql/questionController/insert_question.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void editOtherUserQuestion() {
        String url = createURL(port, "/question/editOtherUserQuestion/{originalQuestionId}");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\questionController\\editedOtherUserQuestion.json");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("originalQuestionId", 1);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("PUT", frontEndUrl));
        ResponseEntity<EditedQuestion> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.PUT,
                entity,
                new ParameterizedTypeReference<EditedQuestion>() {
                });

        EditedQuestion editedQuestion = response.getBody();
        System.out.println(">> Result: " + editedQuestion);
        Assert.assertNotNull(editedQuestion);
        Assert.assertTrue(editedQuestionRepository.findById(editedQuestion.getEditedQuestionId()).isPresent());
    }

    @Test
    @SqlGroup({
            @Sql("/sql/questionController/insert_question.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void viewEditedVersions() {

        String url = createURL(port, "/question/viewEditedVersions/{originalQuestionId}");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\questionController\\viewEditedVersions.json");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("originalQuestionId", 1);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("POST", frontEndUrl));
        ResponseEntity<List<EditedQuestion>> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<List<EditedQuestion>>() {
                });

        List<EditedQuestion> editedQuestions = response.getBody();
        Assert.assertEquals(2, editedQuestions.size());

    }

    @Test
    @SqlGroup({
            @Sql("/sql/questionController/insert_question.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void approveEditedVersion() {

        String url = createURL(port, "/question/approveEditedVersion/{originalQuestionId}");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\questionController\\approveEditedVersion.json");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("originalQuestionId", 1);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("POST", frontEndUrl));
        ResponseEntity<Question> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Question>() {
                });

        Question question = response.getBody();
        Assert.assertEquals("Yêu đơn phương", question.getTitle());
        Assert.assertEquals("Năm nay mình 28 tuổi, đang làm cùng chỗ với crush.", question.getContent());
        // Assert new tags
        Assert.assertEquals("trồng trọt    edited 3.6", question.getTags().get(0).getName());
        Assert.assertEquals("chăn nuôi        edited 3.0", question.getTags().get(1).getName());

        Assert.assertFalse(editedQuestionRepository.findById(1L).isPresent());
        Assert.assertTrue(editedQuestionRepository.findById(2L).isPresent());

    }

    @Test
    @SqlGroup({
            @Sql("/sql/questionController/insert_question.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void reportQuestion() {

        String url = createURL(port, "/question/reportQuestion/{questionId}");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\questionController\\reportQuestion.json");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("questionId", 1);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("POST", frontEndUrl));
        ResponseEntity<Report> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Report>() {
                });

        Report report = response.getBody();
        Assert.assertTrue(reportRepository.findById(report.getReportId()).isPresent());
    }
}