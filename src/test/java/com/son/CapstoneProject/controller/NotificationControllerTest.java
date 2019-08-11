package com.son.CapstoneProject.controller;

import com.son.CapstoneProject.Application;
import com.son.CapstoneProject.entity.Notification;
import com.son.CapstoneProject.entity.Question;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.entity.pagination.NotificationPagination;
import com.son.CapstoneProject.entity.pagination.QuestionPagination;
import com.son.CapstoneProject.repository.NotificationRepository;
import com.son.CapstoneProject.repository.QuestionRepository;
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

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
public class NotificationControllerTest {

    @LocalServerPort
    private int port;

    @Value("${front-end.settings.cross-origin.url}")
    private String frontEndUrl;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @SqlGroup({
            @Sql("/sql/notificationController/deleteNotification.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    @Test
    public void deleteNotification() {
        String url = createURL(port, "/notification/delete/{id}");

        // URI (URL) parameters
        Map<String, String> uriParams = new HashMap<>();
        uriParams.put("id", "1");

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("DELETE", frontEndUrl));
        ResponseEntity<Map<String, String>> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<Map<String, String>>() {
                });

        Map<String, String> questionList = response.getBody();
        System.out.println(">> Result: " + questionList);
        Assert.assertFalse(notificationRepository.findById(1L).isPresent());
    }

    @SqlGroup({
            @Sql("/sql/notificationController/unsubscribe.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    @Test
    public void unsubscribe() {
        String url = createURL(port, "/notification/unsubscribe");

        // URI (URL) parameters
        Map<String, String> uriParams = new HashMap<>();

        String filePath = "src\\test\\resources\\json\\notificationController\\unsubscribe.json";

        String requestBody = CommonTest.readStringFromFile(filePath);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("POST", frontEndUrl));
        ResponseEntity<Notification> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Notification>() {
                });

        Notification notification = response.getBody();
        System.out.println(">> Result: " + notification);
        Question question = questionRepository.findById(1L).get();
        List<AppUser> subscribers = question.getSubscribers();

        boolean is2ndUserUnsub = true;
        for (AppUser subscriber: subscribers) {
            if (subscriber.getUserId().equals(2L)){
                is2ndUserUnsub = false;
            }
        }

        Assert.assertTrue(is2ndUserUnsub);
    }

    @SqlGroup({
            @Sql("/sql/notificationController/viewNotiByUser.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    @Test
    public void viewNotificationsByPageIndex() {
        String url = createURL(port, "/notification/viewNotificationsByPageIndex/{userId}/{pageNumber}");

        // URI (URL) parameters
        Map<String, String> uriParams = new HashMap<>();
        uriParams.put("userId", "1");
        uriParams.put("pageNumber", "0");

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("GET", frontEndUrl));
        ResponseEntity<NotificationPagination> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<NotificationPagination>() {
                });

        NotificationPagination notification = response.getBody();
        System.out.println(">> Result: " + notification);
        Assert.assertTrue(notification.getNotificationsByPageIndex().size() == 3);
    }

    @Test
    @SqlGroup({
            @Sql("/sql/notificationController/viewNotiUnSeen.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void viewNumberOfUnseenNotification() {
        String url = createURL(port, "/notification/viewNumberOfUnseenNotification/{userId}");

        // URI (URL) parameters
        Map<String, String> uriParams = new HashMap<>();
        uriParams.put("userId", "1");

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("GET", frontEndUrl));
        ResponseEntity<Integer> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Integer>() {
                });

        Integer notification = response.getBody();
        System.out.println(">> Result: " + notification);
        Assert.assertTrue(notification == 2);
    }

    @Test
    @SqlGroup({
            @Sql("/sql/notificationController/viewOneNotification.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void viewOneNotification() {
        String url = createURL(port, "/notification/viewOneNotification/{id}");

        // URI (URL) parameters
        Map<String, String> uriParams = new HashMap<>();
        uriParams.put("id", "1");

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("GET", frontEndUrl));
        ResponseEntity<Notification> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<Notification>() {
                });

        Notification notification = response.getBody();
        System.out.println(">> Result: " + notification);
        Assert.assertTrue(notification.isSeen());
    }
}