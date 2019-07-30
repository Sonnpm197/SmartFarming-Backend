package com.son.CapstoneProject.controller.user;

import com.son.CapstoneProject.Application;
import com.son.CapstoneProject.controller.CommonTest;
import com.son.CapstoneProject.entity.login.AppUser;
import com.son.CapstoneProject.entity.pagination.AppUserPagination;
import com.son.CapstoneProject.repository.loginRepository.AppUserRepository;
import com.son.CapstoneProject.repository.loginRepository.SocialUserRepository;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.son.CapstoneProject.controller.CommonTest.createURL;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
public class AppUserControllerTest {

    @LocalServerPort
    private int port;

    @Value("${front-end.settings.cross-origin.url}")
    private String frontEndUrl;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private SocialUserRepository socialUserRepository;

    @Test
    @SqlGroup({
            @Sql("/sql/appUserController/insert_appUser.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void viewUsers() {

        String url = createURL(port, "/userDetail/viewUsers/{pageNumber}");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("pageNumber", 0);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("GET", frontEndUrl));
        ResponseEntity<AppUserPagination> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<AppUserPagination>() {
                });

        AppUserPagination appUserPagination = response.getBody();
        System.out.println(">> Result: " + Arrays.toString(appUserPagination.getAppUsersByPageIndex().toArray()));

    }

    @Test
    @SqlGroup({
            @Sql("/sql/appUserController/insert_appUser.sql"),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void viewTop3UsersByReputation() {

        String url = createURL(port, "/userDetail/viewTop3UsersByReputation");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(null, CommonTest.getHeaders("GET", frontEndUrl));
        ResponseEntity<AppUserPagination> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<AppUserPagination>() {
                });

        AppUserPagination appUserPagination = response.getBody();
        System.out.println();

    }

    @Test
    @SqlGroup({
            @Sql(scripts = "/sql/appUserController/insert_appUser.sql", executionPhase = BEFORE_TEST_METHOD),
            @Sql(scripts = "/sql/clean_database.sql", executionPhase = AFTER_TEST_METHOD)
    })
    public void editProfile() {

        String url = createURL(port, "/userDetail/editProfile/{userId}");

        String requestBody = CommonTest.readStringFromFile("src\\test\\resources\\json\\appUserController\\editProfile.json");

        // URI (URL) parameters
        Map<String, Integer> uriParams = new HashMap<>();
        uriParams.put("userId", 1);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        System.out.println(">>> Testing URI: " + builder.buildAndExpand(uriParams).toUri());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, CommonTest.getHeaders("POST", frontEndUrl));
        ResponseEntity<AppUser> response = CommonTest.getRestTemplate().exchange(
                builder.buildAndExpand(uriParams).toUri(),
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<AppUser>() {
                });

        AppUser appUser = response.getBody();
        System.out.println(">> Result: " + appUser);

        // Check saved SocialInfo + saved url
        Assert.assertEquals("updatedEmail", socialUserRepository.findById(1L).get().getEmail());
        Assert.assertEquals("abcd", appUserRepository.findById(appUser.getUserId()).get().getCvUrl());

    }
}