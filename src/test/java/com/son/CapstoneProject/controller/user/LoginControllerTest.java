package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.Application;
import com.son.CapstoneProject.controller.CommonTest;
import com.son.CapstoneProject.common.entity.login.AppUser;
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
public class LoginControllerTest {

    @LocalServerPort
    private int port;

    @Value("${front-end.settings.cross-origin.url}")
    private String frontEndUrl;

    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    public void loginWithNoSocialExisted() {
        String url = createURL(port, "/login");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\loginController\\socialUser.json");

        // URI (URL) parameters
        Map<String, String> uriParams = new HashMap<>();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("POST", frontEndUrl));
        ResponseEntity<AppUser> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<AppUser>() {
                });

        System.out.println(">> Result: " + response.getBody());
        Assert.assertEquals("USER", response.getBody().getRole());
    }

    @Test
    @SqlGroup({
            @Sql(value = "/sql/questionController/insert_question.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void loginWithSocialExisted() {
        String url = createURL(port, "/login");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\loginController\\socialUser.json");

        // URI (URL) parameters
        Map<String, String> uriParams = new HashMap<>();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("POST", frontEndUrl));
        ResponseEntity<AppUser> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<AppUser>() {
                });

        System.out.println(">> Result: " + response.getBody());
        Assert.assertEquals("USER", response.getBody().getRole());
        Assert.assertEquals(2, appUserRepository.count()); // do not create new
    }
}